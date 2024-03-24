import os
import argparse

from read_data import Data
from mip_model import MIPModel


def location_optimize(file_input_names, file_out_dir):
    max_hub_num = 300
    min_hub_num = 0
    max_hub_delivery_distance = 1600
    fix_hub_cost = 0.1
    for i, file_name in enumerate(file_input_names):
        print(i + 1, file_name)
        file_input_dir_name = '{}/{}'.format(file_input_dir, file_name)
        # 1. 数据处理
        data = Data(file_input_dir_name, file_out_dir, file_name)
        data.data_preprocess()

        # 2. 基于MIP的运筹优化建模求解
        model = MIPModel(data, min_hub_num, max_hub_num, max_hub_delivery_distance, fix_hub_cost)
        model.construct_mip_model()
        model.optimize()


if __name__ == '__main__':
    # parser = argparse.ArgumentParser()
    # parser.add_argument('--input_dir', help='输入文件目录')
    # parser.add_argument('--output_dir', help='输出文件目录')
    # args = parser.parse_args()
    # file_input_dir = "/Users/jinglong/meituan/location/data"  # 文件夹目录
    file_input_dir = "/Users/yuyijun/Desktop/work/private/vw-hub-server/alg/example/in/"  # 文件夹目录
    file_out_dir = '/Users/yuyijun/Desktop/work/private/vw-hub-server/alg/example/out/'

    file_input_names = os.listdir(file_input_dir)  # 得到文件夹下的所有文件名称

    location_optimize(file_input_names, file_out_dir)
