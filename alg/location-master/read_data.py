import json
import math
import os
import time
import pandas as pd
from joblib import Parallel, delayed


class Data:
    def __init__(self, file_input_dir_name, file_out_dir, file_name):
        self.ship_province_hubs = None
        self.train_province_hubs = None
        self.ship_hubs = None
        self.city_location = None
        self.trans_cost = None
        self.train_hubs = None
        self.province_hubs = None
        self.distance_matrix = None
        self.file_input_dir_name = file_input_dir_name
        self.file_out_dir_name = file_out_dir
        self.file_name = file_name

    # @staticmethod
    def data_preprocess(self):
        result = None
        excel_content = pd.read_excel(self.file_input_dir_name, sheet_name=None, index_col=0)
        # 返回所有 Sheets 对象的list
        names = list(excel_content.keys())
        try:
            self.city_location = excel_content["Cost-Volume"].reset_index().set_index("城市")
            # 选特定列
            selected_columns = ["省份", "经度", "纬度", "销量", "合肥出发公路", "合肥出发铁路干线", "合肥出发水路干线"]
            self.city_location = self.city_location.loc[:, selected_columns]
        except:
            result = {
                "success": False,
                "code": "500",
                "message": '请检查 Cost-Volume sheet 名称或列名： "省份", "经度", "纬度", "销量", "合肥出发公路", "合肥出发铁路干线", "合肥出发水路干线"'
            }
            print(json.dumps(result))
            return False
        self.city_location.index = self.city_location.index.str.replace('市', '')
        # 去掉 "省份" 列中的 "省" 和 "市" 字样
        self.city_location["省份"] = self.city_location["省份"].str.replace('省', '').str.replace('市', '')
        # 去掉销量为0的列
        self.city_location = self.city_location[self.city_location["销量"] > 0.5]

        start_time = time.time()
        # distance_matrix 不存在则计算；如果存在了，就直接读取
        try:
            if not os.path.exists('./data/distance_matrix.csv'):
                print("开始计算距离矩阵")
                self.calculate_distance_matrix()
            else:
                self.distance_matrix = pd.read_csv('./data/distance_matrix.csv', index_col=0)
        except:
            result = {
                "success": False,
                "code": "500",
                "message": '请检查距离矩阵计算'
            }
            print(json.dumps(result))
            return False
        end_time = time.time()
        print("计算距离矩阵耗时：", end_time - start_time, "秒")

        # 公路、铁路和水路的费率
        try:
            self.trans_cost = excel_content["Cost-Distribution"].reset_index()
            selected_columns = ["范围起（≥）", "范围止（＜）", "公路费率", "总费用"]
            self.trans_cost = self.trans_cost.loc[:, selected_columns]
            self.trans_cost['边际成本'] = 0
            for i in range(len(self.trans_cost)):
                if i == 0:
                    self.trans_cost.loc[i, '边际成本'] = self.trans_cost.loc[i, '总费用']
                    self.trans_cost.loc[i, "总费用"] = self.trans_cost.loc[i, '总费用']
                else:
                    self.trans_cost.loc[i, '边际成本'] = (self.trans_cost.loc[i, '范围止（＜）'] - self.trans_cost.loc[
                        i, '范围起（≥）']) * self.trans_cost.loc[i, '公路费率'] + self.trans_cost.loc[i - 1, '边际成本']
                    self.trans_cost.loc[i, "总费用"] = 0
        except:
            result = {
                "success": False,
                "code": "500",
                "message": '请检查Cost-Distribution数据格式'
            }
            print(json.dumps(result))
            return False

        # 备选Hubs 信息， 去掉城市重复的行
        self.train_hubs = excel_content["Regional hubs（Train)"].reset_index().drop(["货场名称"], axis=1)
        selected_columns = ["城市", "固定费用", "是否启用"]
        self.train_hubs = self.train_hubs.loc[:, selected_columns][self.train_hubs["是否启用"] == 1]
        self.ship_hubs = excel_content["Regional hubs（Ship)"].reset_index().drop(["货场名称"], axis=1)
        selected_columns = ["城市", "固定费用", "是否启用"]
        self.ship_hubs = self.ship_hubs.loc[:, selected_columns][self.ship_hubs["是否启用"] == 1]
        print("train_hubs 的数量：", self.train_hubs.shape[0])
        print("ship_hubs 的数量：", self.ship_hubs.shape[0])

        # 解析 hub 覆盖范围
        # 定义字典
        self.train_province_hubs = {}
        self.ship_province_hubs = {}
        # 遍历每一行
        for index, row in excel_content["Province-hub"].iterrows():
            # 获取省份
            province = index.replace('省', '').replace('市', '')
            # 获取覆盖的省份列表
            train_hubs = row[["铁路1", "铁路2", "铁路3", "铁路4", "铁路5", "铁路6", "铁路7", "铁路8"]].dropna().tolist()
            ship_hubs = row[["水路1", "水路2", "水路3", "水路4"]].dropna().tolist()
            # 添加到字典
            self.train_province_hubs[province] = train_hubs
            self.ship_province_hubs[province] = ship_hubs

        return True

    # 基于 city_location 中的城市名和经纬度，计算任意两个城市之间的距离矩阵
    # def calculate_distance_matrix(self):
    #     cities = self.city_location.index.tolist()
    #     self.distance_matrix = pd.DataFrame(index=cities, columns=cities)
    #     for i in range(len(cities)):
    #         for j in range(i, len(cities)):
    #             if i == j:
    #                 self.distance_matrix.loc[cities[i], cities[j]] = 30
    #             else:
    #                 lat1, lon1 = self.city_location.loc[cities[i], ["纬度", "经度"]]
    #                 lat2, lon2 = self.city_location.loc[cities[j], ["纬度", "经度"]]
    #                 distance = self.calculate_distance(lat1, lon1, lat2, lon2) + 30
    #                 # 利用对称性填充了下半部分，从而减少了一半的计算量
    #                 self.distance_matrix.loc[cities[i], cities[j]] = distance
    #                 self.distance_matrix.loc[cities[j], cities[i]] = distance
    #
    #     self.distance_matrix.to_csv('./data/distance_matrix.csv')

    def calculate_distance_matrix(self):
        cities = self.city_location.index.tolist()
        self.distance_matrix = pd.DataFrame(index=cities, columns=cities)

        def calculate_distance_ij(i, j):
            if i == j:
                return 30
            else:
                lat1, lon1 = self.city_location.loc[cities[i], ["纬度", "经度"]]
                lat2, lon2 = self.city_location.loc[cities[j], ["纬度", "经度"]]
                distance = self.calculate_distance(lat1, lon1, lat2, lon2) + 30
                return distance

        distances = Parallel(n_jobs=-1)(
            delayed(calculate_distance_ij)(i, j) for i in range(len(cities)) for j in range(i, len(cities)))

        # 根据计算的距离填充距离矩阵
        k = 0
        for i in range(len(cities)):
            for j in range(i, len(cities)):
                self.distance_matrix.loc[cities[i], cities[j]] = distances[k]
                self.distance_matrix.loc[cities[j], cities[i]] = distances[k]
                k += 1

        self.distance_matrix.to_csv('./data/distance_matrix.csv')

    # 计算球面距离
    def calculate_distance(self, lat1, lon1, lat2, lon2):
        # 验证纬度是否在 -90 到 90 之间，经度是否在 -180 到 180 之间
        if not all(-90 <= i <= 90 for i in [lat1, lat2]):
            raise ValueError("纬度必须在 -90 到 90 之间")
        if not all(-180 <= i <= 180 for i in [lon1, lon2]):
            raise ValueError("经度必须在 -180 到 180 之间")
        r = 6371  # Radius of the earth in km
        dlat = math.radians(lat2 - lat1)
        dlon = math.radians(lon2 - lon1)
        a = math.sin(dlat / 2) ** 2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(
            dlon / 2) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        distance = r * c
        return distance

    @staticmethod
    def json_dump(self, json_data):
        article = json.dumps(json_data, ensure_ascii=False, sort_keys=True, indent=4, separators=(',', ':'))
        # encoding:utf-8
        file_object = open(self.file_out_dir + self.file_name.split(".")[0] + ".json", 'w')
        file_object.write(article)
        file_object.close()
        print(article)
        print("---------------------------------------------------------------")
