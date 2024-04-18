# some utils
import math

import pandas as pd


def save_to_file(file_name, contents):
    fh = open(file_name, 'w')
    fh.write(contents)
    fh.close()


def calculate_train_transportation_time(volume, distance):
    if volume <= 49:
        transportation_time = math.ceil(distance / 300) + 1
    elif volume <= 149:
        transportation_time = math.ceil(distance / 350) + 1
    elif volume <= 289:
        transportation_time = math.ceil(distance / 400) + 1
    else:
        transportation_time = math.ceil(distance / 500) + 1

    return transportation_time


def calculate_cost_coef(trans_cost, distance):
    cost = 0
    for i, row in trans_cost.iterrows():
        if row['范围起（≥）'] <= distance < row['范围止（＜）']:
            if i == 0:
                return trans_cost.loc[0, "总费用"]
            cost = previous_cost + (distance - row['范围起（≥）']) * row['公路费率']
        # 边际成本
        previous_cost = row['边际成本']

    return cost


# 干线运输结果处理
def main_trans_result(data, z, x, w):
    city_installed = list()
    print(" ================= 干线成本相关，以及Hub城市 =================")
    for i, h in enumerate(data.train_hubs["城市"]):
        if z[(h, "train")].solution_value() > 0:
            distance = int(data.distance_matrix.get("合肥").get(h, 99) * 10000) / 10000.0
            # hub 总销量
            total_hub_volume = calculate_hub_covered_volume(data, h, "train", x)
            volume_revised = total_hub_volume / 300 * 3
            prepare_time = 3
            transportation_time = calculate_train_transportation_time(volume_revised, distance)

            # hub 总成本
            # print(w[(h, "train")].solution_value(),  total_hub_volume)
            cost = int(data.city_location.loc[h, "合肥出发铁路干线"] * total_hub_volume * 10000) / 10000.0
            h_province = data.city_location["省份"][h]
            city_installed.append([h, h_province, "铁路运输", distance, cost, prepare_time, transportation_time])

    for i, h in enumerate(data.ship_hubs["城市"]):
        if z[(h, "ship")].solution_value() > 0:
            distance = int(data.distance_matrix.get("合肥").get(h, 99) * 10000) / 10000.0
            prepare_time = 3
            transportation_time = math.ceil(distance / 500) + 1

            # hub 总销量
            total_hub_volume = calculate_hub_covered_volume(data, h, "ship", x)
            cost = int(data.city_location.loc[h, "合肥出发水路干线"] * total_hub_volume * 10000) / 10000.0
            h_province = data.city_location["省份"][h]
            city_installed.append([h, h_province, "水路运输", distance, cost, prepare_time, transportation_time])
    # 直发占位用
    city_installed.append(["合肥", "安徽", "公路直发", 0, 0, 0, 0])
    # 输出被选中的城市
    city_installed_df = pd.DataFrame(city_installed,
                                     columns=['中转库城市', "中转库省份", '干线运输方式', '干线距离',
                                              "干线（覆盖城市）总成本",
                                              '干线准备时间',
                                              '干线运输时间'])
    # print(city_installed_df)
    return city_installed_df


def calculate_hub_covered_volume(data, h, hub_type, x):
    covered_volume = 0
    for j, c in enumerate(data.city_location.index):
        if x[(h, c, hub_type)].solution_value() > 0:
            covered_volume += data.city_location.loc[c, "销量"]

    return covered_volume


# 自定义函数用于聚合计算
def custom_agg_function(group):
    # 计算每个组的总销量、总时间
    sales_sum = group['销量'].sum()
    time_sum = group['单台时间'].sum()
    cost_sum = group['总成本/￥'].sum()
    # 计算 sum(销量*时间)
    sales_time_product = (group['销量'] * group['单台时间']).sum()
    # 计算总成本
    cost_sum_product = (group['销量'] * group['单台时间']).sum()
    # 计算单台成本
    unit_cost = cost_sum / sales_sum
    # 计算单位时间
    unit_time = sales_time_product / sales_sum

    return pd.Series({
        '中转库城市': group['中转库城市'].iloc[0],
        '中转库省份': group['中转库省份'].iloc[0],
        '干线运输方式': group['干线运输方式'].iloc[0],
        '总销量': sales_sum,
        '总时间': time_sum,
        '总成本': cost_sum,
        'sum(销量*时间）': sales_time_product,
        '单台成本': unit_cost,
        '单台时间': unit_time
    })

    # 按中转库城市、中转库省份和干线运输方式分组并聚合数据
    grouped_df = group.groupby(['中转库城市', '中转库省份', '干线运输方式']).apply(custom_agg_function)
    return grouped_df
