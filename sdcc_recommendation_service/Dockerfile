#Dockerfile to containerize the recommendation service

FROM python:3.8-slim
RUN apt-get update -qqy && \
	apt-get -qqy install wget g++ && \
	rm -rf /var/lib/apt/lists/*
# show python logs as they occur
ENV PYTHONUNBUFFERED=0

# get packages
WORKDIR /recommendationservice
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt

# add files into working directory
COPY . .

# set listen port
ENV PORT "7000"
EXPOSE 7000

ENTRYPOINT ["python", "/recommendationservice/src/RecommendationServer.py"]
