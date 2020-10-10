FROM clojure:tools-deps

WORKDIR /app

ARG TOKEN

COPY src src
COPY deps.edn deps.edn
COPY settings.xml /root/.m2/settings.xml
COPY repo.edn /root/.clojure/deps.edn

RUN clj -P

CMD ["clj", "-M", "-m", "app.core"]
