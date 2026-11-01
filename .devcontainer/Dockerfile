FROM eclipse-temurin:21-jdk AS dev

ARG HOST_UID
ARG HOST_GID
RUN apt-get update \
    && apt-get install -y --no-install-recommends git openssh-client procps \
    && rm -rf /var/lib/apt/lists/* \
    && test "$HOST_UID" -gt 0 && test "$HOST_GID" -gt 0 \
    && groupadd --non-unique --gid "$HOST_GID" dev \
    && useradd --non-unique --uid "$HOST_UID" --gid dev \
        --create-home --shell /bin/bash dev

ENV HOME=/home/dev
USER dev
WORKDIR /workspace
CMD ["sleep", "infinity"]

FROM eclipse-temurin:21-jre AS server

COPY start-server.sh /usr/local/bin/start-server
WORKDIR /app
EXPOSE 45000
ENTRYPOINT ["/bin/sh", "/usr/local/bin/start-server"]
