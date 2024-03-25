# 使用官方的基础镜像
FROM ubuntu:20.04

# 安装JDK 1.8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 设置JDK 1.8为默认JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64

# 安装Python 3.9
RUN apt-get update && \
    apt-get install -y python3.9 python3.9-distutils && \
    rm -rf /var/lib/apt/lists/*

# 设置Python 3.9为默认Python版本
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.9 1 && \
    update-alternatives --install /usr/bin/python python /usr/bin/python3.9 1

# 安装pip
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://bootstrap.pypa.io/get-pip.py && \
    python3.9 get-pip.py && \
    rm get-pip.py

# 设置环境变量
ENV PATH $PATH:/usr/local/bin:/usr/bin/python3.9

ADD alg/ /py
ADD src/main/resources/static/ /data/static/
RUN pip install --no-cache-dir --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple
RUN pip install -r /py/location-master/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

ADD target/vw-hub-server-1.0-SNAPSHOT.jar app.jar
ADD src/main/resources/ /config

EXPOSE 8083
ENTRYPOINT ["java","-jar", "app.jar","--spring.profiles.active=prod"]