# Cielo LIO SDK Libraries

Place the Cielo LIO Order Manager SDK artifacts in this directory before building.

Download them from the official sample project:
https://github.com/DeveloperCielo/LIO-SDK-Sample-Integracao-Local

Typical files copied from the sample `app/libs` folder:

- `order-manager-*.aar`
- `orders-domain-*.aar`
- `event-tracker-*.aar`
- Any required companion `.jar` files

After copying the files, update `CREDENTIALS_CLIENT_ID` and `CREDENTIALS_ACCESS_TOKEN`
in `app/build.gradle.kts` with your Portal do Desenvolvedor credentials.

Install the Cielo LIO Emulator on the test device to exercise checkout flows:
https://developercielo.github.io/manual/lio-local
