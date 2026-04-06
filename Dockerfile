FROM ghcr.io/cirruslabs/android-sdk:34

WORKDIR /workspace

# Copy the whole project
COPY . .

# Gradle wrapper needs execute permission
RUN chmod +x ./gradlew

# Quick sanity check at image build time
RUN ./gradlew --no-daemon -Dorg.gradle.java.home="$JAVA_HOME" help

# Default command for local CI-style runs
CMD ["sh", "-c", "./gradlew --no-daemon -Dorg.gradle.java.home=\"$JAVA_HOME\" clean help"]
