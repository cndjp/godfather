FROM openjdk:11 as builder

# sbt versionを固定
ARG SBT_VERSION=1.2.8

# Install sbt
RUN \
    curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb

# 必要なdebパッケージのインストール
RUN set -eux; \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    sbt && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir /opt/app
COPY . /opt/app
WORKDIR /opt/app

# コンパイル
RUN sbt sbtVersion
RUN sbt assembly

FROM openjdk:11-jre as runner
ARG EVENT_URL
ENV DEBIAN_FRONTEND=noninteractive
RUN set -eux; \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        tzdata && \
        rm -rf /var/lib/apt/lists/*

RUN cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    echo Asia/Tokyo > /etc/timezone

RUN mkdir /opt/app
WORKDIR /opt/app
COPY --from=builder /opt/app/target/scala-2.12/godfather.jar .

RUN groupadd --non-unique --gid 23456 cndjp
RUN useradd --non-unique --system --uid 12345 --gid 23456 qicoo
USER qicoo

CMD java -jar /opt/app/godfather.jar -event-url $EVENT_URL