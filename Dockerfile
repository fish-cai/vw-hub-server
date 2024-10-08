FROM ubuntu:20.04

# 设置环境变量
ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=C.UTF-8

RUN sed -i 's@http://archive.ubuntu.com/ubuntu/@http://mirrors.tuna.tsinghua.edu.cn/ubuntu/@' /etc/apt/sources.list


# 安装基本依赖
RUN apt-get update \
    && apt-get install -y \
        software-properties-common \
        curl \
        wget \
        unzip \
        sudo \
        gnupg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 安装 JDK 1.8
RUN apt-get update \
    && apt-get install -y \
        openjdk-8-jdk \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN apt-get autoremove -y && \
        rm -rf /usr/lib/python3 /usr/local/bin/python3 /usr/local/bin/python

# 安装 Python 3.9
RUN apt-get update \
    && apt-get install -y \
        python3.9 \
        python3.9-distutils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s /usr/bin/python3.9 /usr/bin/python \
    && rm -f /usr/bin/python3 \
    && ln -s /usr/bin/python3.9 /usr/bin/python3

# 安装 pip
RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py \
    && python3 get-pip.py \
    && rm get-pip.py

# 设置 Python 默认版本
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3 1

ADD alg/ /py
#COPY vwhub.db /data/vwhub.db
RUN pip install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple
RUN pip install -r /py/location-master/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

ADD target/vw-hub-server-1.0-SNAPSHOT.jar app.jar
RUN mkdir /model
ADD src/main/resources/ /config

EXPOSE 8080
ENTRYPOINT ["java","-jar", "app.jar","--spring.profiles.active=prod"]