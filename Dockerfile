FROM clojure:tools-deps

WORKDIR /app

ARG TOKEN

COPY src src
COPY deps.edn deps.edn

RUN clj -P

CMD ["clj", "-M", "-m", "app.core"]
