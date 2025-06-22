FROM eclipse-temurin:17-jdk-jammy

ENV ANDROID_SDK_ROOT /opt/android-sdk
ENV PATH $PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

RUN apt-get update && \
    apt-get install -y wget unzip git && \
    rm -rf /var/lib/apt/lists/*

RUN wget -O cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" && \
    mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    unzip -q cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools && \
    mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest && \
    rm cmdline-tools.zip

RUN yes | sdkmanager --licenses && \
    sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

RUN curl -Ls "https://sh.jbang.dev" | bash -s - app install --fresh --force jbang

WORKDIR /app