FROM navikt/java:14
COPY target/*-jar-with-dependencies.jar app.jar
