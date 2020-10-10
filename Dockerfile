FROM clojure:tools-deps

WORKDIR /app

ENV TOKEN ${TOKEN}

COPY src src
COPY deps.edn deps.edn
COPY settings.xml ~/.m2/settings.xml
COPY repo.edn ~/.clojure/deps.edn

RUN clj -P

CMD ["clj", "-M", "-m", "app.core"]
