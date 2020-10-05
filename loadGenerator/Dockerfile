FROM python:3-slim as base

FROM base as builder

RUN apt-get -qq update \
    && apt-get install -y --no-install-recommends \
        g++

COPY requirements.txt .

RUN export PYTHONPATH=/usr/local/lib/python3.8/site-packages
RUN pip install --prefix="/install" -r requirements.txt

FROM base
COPY --from=builder /install /usr/local

COPY . .

RUN export PYTHONPATH=/usr/local/lib/python3.8/site-packages
RUN export PATH=/usr/local/bin
RUN chmod +x ./loadgen.sh
RUN apt-get -qq update \
    && apt-get install -y --no-install-recommends \
        curl
ENTRYPOINT ./loadgen.sh