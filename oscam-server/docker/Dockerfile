# =============================================================================
#  OSCam - build ze zrodel (multi-stage), obraz produkcyjny na Alpine.
# -----------------------------------------------------------------------------
#  Buduje OSCam z oficjalnego repozytorium SVN/Git na etapie "builder",
#  a nastepnie kopiuje sam binarny plik do minimalnego obrazu runtime.
#
#  Build:  docker build -t oscam:latest ./docker
# =============================================================================

# --- Etap 1: kompilacja ------------------------------------------------------
FROM alpine:3.20 AS builder

ARG OSCAM_REPO=https://git.streamboard.tv/common/oscam.git
ARG OSCAM_REF=master

RUN apk add --no-cache \
        build-base cmake git \
        libusb-dev pcsc-lite-dev \
        openssl-dev zlib-dev

WORKDIR /src
RUN git clone --depth 1 --branch "${OSCAM_REF}" "${OSCAM_REPO}" oscam || \
    git clone --depth 1 "${OSCAM_REPO}" oscam

WORKDIR /src/oscam
# Wlacz najczesciej uzywane protokoly i wsparcie webif/ssl.
RUN mkdir -p build && cd build && \
    cmake -DWEBIF=1 -DHAVE_LIBUSB=1 -DWITH_SSL=1 -DCS_ANTICASC=1 .. && \
    make -j"$(nproc)" && \
    strip oscam

# --- Etap 2: runtime ---------------------------------------------------------
FROM alpine:3.20

RUN apk add --no-cache \
        libusb pcsc-lite-libs openssl zlib \
        tzdata ca-certificates && \
    addgroup -S oscam && adduser -S -G oscam oscam && \
    mkdir -p /etc/oscam /var/log/oscam && \
    chown -R oscam:oscam /etc/oscam /var/log/oscam

COPY --from=builder /src/oscam/build/oscam /usr/local/bin/oscam

USER oscam
EXPOSE 8888 12000 15000

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -qO- --no-check-certificate https://127.0.0.1:8888/ >/dev/null 2>&1 \
        || wget -qO- http://127.0.0.1:8888/ >/dev/null 2>&1 || exit 1

# -b = tryb foreground (bez demonizacji, wymagane w kontenerze)
ENTRYPOINT ["/usr/local/bin/oscam", "-b", "-c", "/etc/oscam"]
