import json
import os
import argparse

import utils
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
        utils.clear_directory(file_out_dir)
        data = Data(file_input_dir_name, file_out_dir, file_name)
        if data.data_preprocess() is False:
            return

        # 2. 基于MIP的运筹优化建模求解
        model = MIPModel(data, min_hub_num, max_hub_num, max_hub_delivery_distance, fix_hub_cost)
        block_solution = list()
        for it in range(1, 25):
            print("第" + str(it) + "次求解")
            block = list()
            if len(block_solution) > 0:
                if it - 1 < len(block_solution):
                    block.append(block_solution[it-2])
                else:
                    block.append(block_solution[0])
                    block.append(block_solution[it - len(block_solution)])

            model.construct_mip_model(block)
            if it == 1:
                block_solution = model.optimize(it)
            else:
                model.optimize(it)

        utils.rename_files_and_select_top_20(file_out_dir)
        # 3. 结果状态输出
        result = {
            "success": True,
            "code": "200",
            "message": "优化完成"
        }
        print(json.dumps(result))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--input_dir', help='输入文件目录')
    parser.add_argument('--output_dir', help='输出文件目录')
    args = parser.parse_args()
    # file_input_dir = "/Users/jinglong/meituan/location/data"  # 文件夹目录
    # file_input_dir = "/Users/caiwenlin/java/vw-hub-server/alg/example/in/"  # 文件夹目录
    # file_out_dir = '/Users/caiwenlin/java/vw-hub-server/alg/example/out/'
    file_input_dir = args.input_dir
    file_out_dir = args.output_dir
    file_input_names = os.listdir(file_input_dir)  # 得到文件夹下的所有文件名称

    location_optimize(file_input_names, file_out_dir)