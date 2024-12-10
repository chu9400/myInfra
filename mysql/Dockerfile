FROM mysql:8.0-debian

ENV MYSQL_ROOT_PASSWORD=qwer1234
ENV MYSQL_USER=newuser
ENV MYSQL_PASSWORD=qwer1234

COPY ./init.sql /docker-entrypoint-initdb.d/init.sql