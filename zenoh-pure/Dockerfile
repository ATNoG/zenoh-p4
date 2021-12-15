FROM opennetworking/mn-stratum:20.12 as runtime

RUN install_packages curl ca-certificates python3-pip python3-setuptools 

RUN apt update -y
RUN apt upgrade -y

RUN apt install wget build-essential libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev libffi-dev zlib1g-dev git apt-transport-https -y

RUN wget https://www.python.org/ftp/python/3.9.4/Python-3.9.4.tgz 
RUN tar xzf Python-3.9.4.tgz

WORKDIR Python-3.9.4 

RUN ./configure --enable-optimizations 
RUN make altinstall


WORKDIR /
RUN python3 --version
RUN python3.9 --version


RUN python3.9 -m pip install --upgrade pip
RUN python3.9 -m pip install --upgrade setuptools
RUN cat /root/.profile
RUN sed -i 's/mesg n || true//g' /root/.profile
RUN echo "tty -s && mesg n" >> /root/.profile
RUN cat /root/.profile
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs -o rustup.sh && sh rustup.sh -y
SHELL ["bash", "-lc"] #. ./$HOME/.cargo/env
RUN rustup default nightly

#RUN git clone --branch branch_0.5.0-beta.8 https://github.com/eclipse-zenoh/zenoh.git

#RUN cd zenoh && cargo build --release --all-targets
RUN echo "deb [trusted=yes] https://download.eclipse.org/zenoh/zenoh/0.5.0-beta.8/ /" | tee -a /etc/apt/sources.list > /dev/null
RUN apt update -y
RUN apt install zenoh -y

RUN pip3.9 install --no-cache-dir eclipse-zenoh==0.5.0-b8


RUN pip3.9 install scapy

WORKDIR /root
COPY ./stratum.py ./stratum.py
