import math

import numpy as np
import pandas as pd
from ortools.linear_solver import pywraplp

import utils


class MIPModel:
    def __init__(self, data, min_hub_num, max_hub_num, max_hub_delivery_distance, fix_hub_cost):
        self.bigM = 10e12
        self.model_name = "location model"
        self.data = data
        self.max_hub_num = max_hub_num
        self.min_hub_num = min_hub_num
        self.max_hub_delivery_distance = max_hub_delivery_distance
        self.fix_hub_cost = fix_hub_cost
        self.x = None
        self.obj1 = None
        self.obj2 = None
        self.solver = None
        self.hubs = './out/' + "Hub cities" + '.csv'
        self.result = './out/' + "Location Result"

    def construct_mip_model(self):
        # Create the mip solver with the SCIP backend.
        solver = pywraplp.Solver.CreateSolver('SCIP')
        # solver = pywraplp.Solver.CreateSolver('CPLEX')
        infinity = solver.infinity()

        # Variables :
        train_hub_city = dict()  # 新增变量，表示一个城市是否被访问
        ship_hub_city = dict()  # 新增变量，表示一个城市是否被访问
        highway_hub_city = dict()
        for i, t in enumerate(self.data.train_hubs["城市"]):
            demand = self.data.city_location.loc[t, "销量"]
            if t == "合肥" or t == "大理":
                train_hub_city[t] = solver.IntVar(1, 1, 'train_hub_%i' % i)
            elif demand < 0.5:
                train_hub_city[t] = solver.IntVar(0, 0, 'train_hub_%i' % i)
            else:
                train_hub_city[t] = solver.IntVar(0, 1, 'train_hub_%i' % i)
        for i, s in enumerate(self.data.ship_hubs["城市"]):
            demand = self.data.city_location.loc[s, "销量"]
            if s == "合肥":
                ship_hub_city[s] = solver.IntVar(1, 1, 'ship_hub_%i' % i)
            elif demand < 0.5:
                ship_hub_city[s] = solver.IntVar(0, 0, 'train_hub_%i' % i)
            else:
                ship_hub_city[s] = solver.IntVar(0, 1, 'train_hub_%i' % i)

        for i, highway in enumerate(self.data.city_location.index):
            if highway == "合肥":
                highway_hub_city[highway] = solver.IntVar(1, 1, 'highway_hub_%i' % i)
            else:
                highway_hub_city[highway] = solver.IntVar(0, 1, 'highway_hub_%i' % i)

        x = dict()
        y = dict()
        # train
        for i, h in enumerate(self.data.train_hubs["城市"]):
            for j, c in enumerate(self.data.city_location.index):
                if_access = 0
                h_province = self.data.city_location["省份"][h]
                c_province = self.data.city_location["省份"][c]
                # hub 对应的省份覆盖了哪些范围，不可超过
                if c_province in self.data.train_province_hubs[h_province]:
                    if_access = 1
                x[(h, c, "train")] = solver.IntVar(0, int(if_access), 'x_%i_%i_train' % (i, j))
                y[(h, c, "train")] = solver.IntVar(0, infinity, 'y_%i_%i_train' % (i, j))
                solver.Add(y[(h, c, "train")] <= self.bigM * x[(h, c, "train")])

        # ship
        for i, h in enumerate(self.data.ship_hubs["城市"]):
            for j, c in enumerate(self.data.city_location.index):
                if_access = 0
                h_province = self.data.city_location["省份"][h]
                c_province = self.data.city_location["省份"][c]
                # hub 对应的省份覆盖了哪些范围，不可超过
                if c_province in self.data.ship_province_hubs[h_province]:
                    if_access = 1
                x[(h, c, "ship")] = solver.IntVar(0, int(if_access), 'x_%i_%i_ship' % (i, j))
                y[(h, c, "ship")] = solver.IntVar(0, infinity, 'y_%i_%i_ship' % (i, j))
                solver.Add(y[(h, c, "ship")] <= self.bigM * x[(h, c, "ship")])

        # direct
        for i, s in enumerate(self.data.city_location.index):
            demand = self.data.city_location.loc[s, "销量"]
            x[s] = solver.IntVar(0, 1, 'x_合肥_%i' % i)
            y[s] = solver.IntVar(0, 2 * int(demand), 'y_合肥_%i' % i)
            solver.Add(y[s] <= self.bigM * x[s])

        print('Number of variables =', solver.NumVariables())

        # Constraints
        # cons.1 最多选择 N 个 Station，即所有的 self.data.city_location.index，判断任何一个城市train 或者 ship 是否访问
        # 如果有访问则统计为1，约束所有被访问的城市数量不超过self.N
        for h in self.data.train_hubs["城市"]:
            solver.Add(train_hub_city[h] * self.bigM >= solver.Sum(
                [x[(h, c, "train")] for c in self.data.city_location.index]))
        for h in self.data.ship_hubs["城市"]:
            solver.Add(ship_hub_city[h] * self.bigM >= solver.Sum(
                [x[(h, c, "ship")] for c in self.data.city_location.index]))
        for h in self.data.city_location.index:
            solver.Add(highway_hub_city[h] * self.bigM >= solver.Sum([x[h] for h in self.data.city_location.index]))
        solver.Add(self.min_hub_num <= solver.Sum([train_hub_city[hub] for hub in self.data.train_hubs["城市"]]) +
                   solver.Sum([ship_hub_city[hub] for hub in self.data.ship_hubs["城市"]]))
        solver.Add(solver.Sum([train_hub_city[hub] for hub in self.data.train_hubs["城市"]]) +
                   solver.Sum([ship_hub_city[hub] for hub in self.data.ship_hubs["城市"]]) <= self.max_hub_num)

        # cons.2 所有城市的需求都要被Hub满足
        for c in self.data.city_location.index:
            demand = self.data.city_location.loc[c, "销量"]
            solver.Add(solver.Sum([y[(h, c, "train")] for h in self.data.train_hubs["城市"]] +
                                  [y[(h, c, "ship")] for h in self.data.ship_hubs["城市"]]) +
                       y[c] >= demand)

        # cons.3 距离小于1000公里
        for c in self.data.city_location.index:
            for h in self.data.train_hubs["城市"]:
                distance = self.data.distance_matrix.get(h).get(c, 99999)
                solver.Add(x[h, c, "train"] * distance <= self.max_hub_delivery_distance)

            for h in self.data.ship_hubs["城市"]:
                distance = self.data.distance_matrix.get(h).get(c, 99999)
                solver.Add(x[h, c, "ship"] * distance <= self.max_hub_delivery_distance)

        print('Number of constraints =', solver.NumConstraints())

        # objective
        objective = solver.Objective()
        # 固定成本
        for h in self.data.train_hubs["城市"]:
            objective.SetCoefficient(train_hub_city[h], self.fix_hub_cost)
        for h in self.data.ship_hubs["城市"]:
            objective.SetCoefficient(ship_hub_city[h], self.fix_hub_cost)
        for h in self.data.city_location.index:
            objective.SetCoefficient(x[h], self.fix_hub_cost)

        # 运输成本
        for h in self.data.train_hubs["城市"]:
            for c in self.data.city_location.index:
                total_train_cost = self.calculate_total_cost(h, c, "合肥出发铁路干线")
                objective.SetCoefficient(x[(h, c, "train")], total_train_cost)
        for h in self.data.ship_hubs["城市"]:
            for c in self.data.city_location.index:
                total_ship_cost = self.calculate_total_cost(h, c, "合肥出发水路干线")
                objective.SetCoefficient(x[(h, c, "ship")], total_ship_cost)
        for h in self.data.city_location.index:
            total_direct_cost = self.data.city_location.loc[h, "合肥出发公路"] * self.data.city_location.loc[h, "销量"]
            objective.SetCoefficient(x[h], total_direct_cost)

        objective.SetMinimization()

        # save lp model file
        # lp_model = solver.ExportModelAsLpFormat(False)
        # utils.save_to_file('./model/model.lp', lp_model)

        # 设置日志输出到控制台
        # solver.EnableOutput()

        self.solver = solver
        self.x = x
        self.train_hub_city = train_hub_city
        self.ship_hub_city = ship_hub_city

    def optimize(self):
        x = self.x
        train_hub_city = self.train_hub_city
        ship_hub_city = self.ship_hub_city
        # solve
        for it in range(1, 21):
            self.reoptimize(it)
            solver = self.solver
            print("第" + str(it) + "次求解")
            # 添加约束以排除当前解
            exclude = solver.Constraint(solver.Objective().Value() - 1 + (it - 1) * 1000, solver.infinity())
            # 固定成本
            for h in self.data.train_hubs["城市"]:
                exclude.SetCoefficient(train_hub_city[h], self.fix_hub_cost)
            for h in self.data.ship_hubs["城市"]:
                exclude.SetCoefficient(ship_hub_city[h], self.fix_hub_cost)

            # 运输成本
            for h in self.data.train_hubs["城市"]:
                for c in self.data.city_location.index:
                    volume = self.data.city_location.loc[c, "销量"]
                    total_train_cost = self.calculate_total_cost(h, c, "合肥出发铁路干线") * volume
                    exclude.SetCoefficient(x[(h, c, "train")], total_train_cost)
            for h in self.data.ship_hubs["城市"]:
                for c in self.data.city_location.index:
                    volume = self.data.city_location.loc[c, "销量"]
                    total_ship_cost = self.calculate_total_cost(h, c, "合肥出发水路干线") * volume
                    exclude.SetCoefficient(x[(h, c, "ship")], total_ship_cost)
            for h in self.data.city_location.index:
                volume = self.data.city_location.loc[h, "销量"]
                total_direct_cost = self.data.city_location.loc[h, "合肥出发公路"] * volume
                exclude.SetCoefficient(x[h], total_direct_cost)

    def reoptimize(self, it):
        solver = self.solver
        status = solver.Solve()
        print(
            " -------------------------------------- result ----------------------------------------")
        if status == pywraplp.Solver.OPTIMAL:

            print('Objective value =', int(solver.Objective().Value()))
            print('Time = ', solver.WallTime(), ' milliseconds')
            print('Problem solved in %f milliseconds' % solver.wall_time())
            print('Problem solved in %d iterations' % solver.iterations())
            print('Problem solved in %d branch-and-bound nodes' % solver.nodes())
            # 干线运输结果处理
            city_installed_df = utils.main_trans_result(self.data, self.train_hub_city, self.ship_hub_city, self.x)

            # 末端配送结果的处理
            cover_city_df = self.delivery_result()

            # 合并结果
            self.merged_result(city_installed_df, cover_city_df, it)

        else:
            print('The problem does not have an optimal solution.')

    # 计算总成本
    def calculate_total_cost(self, hub, city, main_line_type_from_hefei):
        volume = self.data.city_location.loc[hub, "销量"]
        try:
            cost_1 = self.data.city_location.loc[hub, main_line_type_from_hefei]
        except KeyError:
            raise KeyError("Data not found for the given parameters: "
                           "hub={}, main_line_type_from_hefei={}".format(hub,
                                                                         main_line_type_from_hefei))
        distance = self.data.distance_matrix.get(hub).get(city, 99999)
        cost_2 = utils.calculate_cost_coef(self.data.trans_cost, distance)
        return int(cost_1 * 100 + cost_2 * 100) * volume / 100.0

    def calculate_distribute_cost(self, city, distance):
        volume = self.data.city_location.loc[city, "销量"]
        cost = utils.calculate_cost_coef(self.data.trans_cost, distance)
        return int(cost * volume * 100) / 100.0

    # 末端配送计算结果
    def delivery_result(self):
        print(" ================= 覆盖的城市 =================")
        cover_city = list()
        for i, h in enumerate(self.data.train_hubs["城市"]):
            for j, c in enumerate(self.data.city_location.index):
                if self.x[(h, c, "train")].solution_value() > 0:
                    distance = int(self.data.distance_matrix.get(h).get(c, 99999) * 10000) / 10000.0
                    volume = self.data.city_location.loc[c, "销量"].round(4)
                    volume_revised = (self.data.city_location.loc[c, "销量"] / 300).round(4)
                    prepare_time = 1 if volume_revised >= 4 else 2
                    transportation_time = math.ceil(distance / 500)
                    cost = self.calculate_distribute_cost(c, distance)
                    c_province = self.data.city_location["省份"][c]
                    # 直发距离、成本、准备时间、运输时间
                    dis = int(self.data.distance_matrix.get("合肥").get(c, 99999) * 10000) / 10000.0
                    cos = int(self.data.city_location.loc[c, "合肥出发公路"] * volume * 10000) / 10000.0
                    prep = 1
                    tran = math.ceil(dis / 500)
                    # 干线成本
                    main_cost = int(self.data.city_location.loc[h, "合肥出发铁路干线"] * volume * 10000) / 10000.0
                    cover_city.append(
                        [main_cost, h, c, c_province, "公路运输", distance, cost, volume, prepare_time,
                         transportation_time, dis,
                         cos, prep, tran])

        for i, h in enumerate(self.data.ship_hubs["城市"]):
            for j, c in enumerate(self.data.city_location.index):
                if self.x[(h, c, "ship")].solution_value() > 0:
                    distance = int(self.data.distance_matrix.get(h).get(c, 99999) * 10000) / 10000.0
                    volume = self.data.city_location.loc[c, "销量"].round(4)
                    volume_revised = (self.data.city_location.loc[c, "销量"] / 300).round(4)
                    prepare_time = 1 if volume_revised >= 4 else 2
                    transportation_time = math.ceil(distance / 500)
                    cost = self.calculate_distribute_cost(c, distance)
                    c_province = self.data.city_location["省份"][c]
                    # 直发距离、成本、准备时间、运输时间
                    dis = int(self.data.distance_matrix.get("合肥").get(c, 99999) * 10000) / 10000.0
                    cos = int(self.data.city_location.loc[c, "合肥出发公路"] * volume * 10000) / 10000.0
                    prep = 1
                    tran = math.ceil(dis / 500)
                    # 干线成本
                    main_cost = int(self.data.city_location.loc[h, "合肥出发水路干线"] * volume * 10000) / 10000.0
                    cover_city.append(
                        [main_cost, h, c, c_province, "公路运输", distance, cost, volume, prepare_time,
                         transportation_time, dis,
                         cos, prep, tran])

        for i, h in enumerate(self.data.city_location.index):
            if self.x[h].solution_value() > 0:
                distance = int(self.data.distance_matrix.get("合肥").get(h, 99999) * 10000) / 10000.0
                # 公路直发：按【准备时间=1天】+【在途时间=roundup((GPS距离/500),0）】计算
                prepare_time = 1
                transportation_time = math.ceil(distance / 500)
                volume = self.data.city_location.loc[h, "销量"].round(4)
                cost = int(self.data.city_location.loc[h, "合肥出发公路"] * volume * 10000) / 10000.0
                h_province = self.data.city_location["省份"][h]
                # 直发距离、成本、准备时间、运输时间
                dis = int(self.data.distance_matrix.get("合肥").get(h, 99999) * 10000) / 10000.0
                cos = int(self.data.city_location.loc[h, "合肥出发公路"] * volume * 10000) / 10000.0
                prep = 1
                tran = math.ceil(dis / 500)
                cover_city.append(
                    [0, "合肥", h, h_province, "公路运输", 0, 0, volume, 0, 0, dis, cos, prep, tran])

        cover_city_df = pd.DataFrame(cover_city,
                                     columns=["干线成本", '中转库城市', '覆盖城市', '覆盖省份', '末端运输方式',
                                              '末端距离',
                                              "末端成本", "销量", '末端准备时间',
                                              '末端运输时间', "直发距离", "直发成本", "直发准备时间", "直发运输时间"])

        return cover_city_df

    def merged_result(self, city_installed_df, cover_city_df, it):
        # 数据合并
        merged_df = pd.merge(city_installed_df, cover_city_df, on='中转库城市', how='inner')

        # 计算总距离、总时间和总成本
        merged_df['总距离/km'] = np.where(merged_df['干线成本'] <= 0.1, merged_df['直发距离'],
                                          (merged_df['干线距离'] + merged_df['末端距离']).round(2))
        merged_df['总时间/days'] = np.where(merged_df['干线成本'] <= 0.1,
                                            merged_df['直发准备时间'] + merged_df['直发运输时间'],
                                            merged_df['末端准备时间'] + merged_df['末端运输时间'] +
                                            merged_df['干线准备时间'] + merged_df['干线运输时间'])
        merged_df['总成本/￥'] = np.where(merged_df['干线成本'] <= 0.1, merged_df['直发成本'],
                                         (merged_df['干线成本'] + merged_df['末端成本']).round(2))

        # 单台成本
        merged_df['单台成本'] = merged_df['总成本/￥'] / merged_df['销量']
        merged_df['单台时间'] = merged_df['总时间/days']    # 单台时间 = 总时间/days

        # 展示处理后的DataFrame
        print(merged_df)

        # 计算summary 结果表
        # summary_df = utils.group_data(merged_df.copy())
        summary_df = merged_df.copy().groupby(['中转库城市', '中转库省份', '干线运输方式']).apply(utils.custom_agg_function)

        # # Save all results to a single Excel file
        self.result = self.data.file_out_dir_name + "/Location_Output_" + str(it) + '.csv'
        writer = pd.ExcelWriter(self.result.replace('.csv', '.xlsx'))  # Use ExcelWriter for multi-sheet output

        # Save merged_df to the first sheet (default name "Sheet1")
        merged_df.to_excel(writer, sheet_name='Location', index=False, encoding='utf_8_sig')
        print('Result (merged_df) saved to sheet "Location" in:', self.result.replace('.csv', '.xlsx'))

        # Save summary_df to a separate sheet named "Summary"
        summary_df.to_excel(writer, sheet_name='Summary', index=False)
        print('Summary (summary_df) saved to sheet "Summary" in:', self.result.replace('.csv', '.xlsx'))

        writer.save()  # Close the writer to finalize the Excel file
