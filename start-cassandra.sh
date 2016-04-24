docker stop blog-cassandra || true
docker rm blog-cassandra || true
docker run --name blog-cassandra \
        --publish 9042:9042 \
        --publish 7000:7000 \
        --publish 7199:7199 \
        --publish 9160:9160 \
        -d cassandra:2.2.5
