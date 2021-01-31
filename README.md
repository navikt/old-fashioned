# Old Fashioned STS

This service is a Token Exchange server, which accepts Azure AD tokens for NAV-ansatte, and
exchanges them for ditto OpenAM tokens.

The reason this STS exists, is that we want to be able to login with Azure AD, and still communicate
with legacy backend services that only accept OpenAM tokens.

![Build Success](/docs/drink.jpg?raw=true " Build Success")

### How to build

- dash of angostura bitters
- a little bit of water
- 1 suger cube (crush it)
- ice cubes
- 4.5 cl Bourbon Whisky

Garnish with orange peel.
Run `mvn clean install`.

