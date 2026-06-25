<#-- Login theme for trading-app. Self-contained: no parent template import. -->

<!DOCTYPE html>
<html lang="${(locale.currentLanguageTag)!'en'}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("doLogIn")} - Trading App</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/styles.css">
</head>
<body class="login-pf">

<div id="kc-header">
    <div id="kc-header-wrapper"></div>
</div>

<div id="kc-content">
    <div id="kc-content-wrapper">

        <h1 id="kc-page-title"><span class="brand">trading-app</span> ${msg("doLogIn")}</h1>

        <#if messagesPerField.existsError('username','password')>
            <div id="input-error" class="pf-m-danger" role="alert">
                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
            </div>
        </#if>

        <div id="kc-form-wrapper">
            <form id="kc-form-login" action="${url.loginAction}" method="post" novalidate>
                <div class="pf-c-form__group">
                    <label for="username" class="pf-c-form__label">${msg("usernameOrEmail")}</label>
                    <input tabindex="1"
                           id="username"
                           class="pf-c-form-control"
                           name="username"
                           value="${(login.username!'')}"
                           type="text"
                           autofocus
                           autocomplete="username" />
                </div>

                <div class="pf-c-form__group">
                    <label for="password" class="pf-c-form__label">${msg("password")}</label>
                    <input tabindex="2"
                           id="password"
                           class="pf-c-form-control"
                           name="password"
                           type="password"
                           autocomplete="current-password" />
                </div>

                <#if realm.rememberMe>
                    <div class="pf-c-form__group pf-m-inline">
                        <label class="pf-c-check__label">
                            <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" />
                            ${msg("rememberMe")}
                        </label>
                    </div>
                </#if>

                <div id="kc-form-buttons" class="pf-c-form__group">
                    <button tabindex="4"
                            class="pf-c-button pf-m-primary"
                            name="login"
                            id="kc-login"
                            type="submit">
                        ${msg("doLogIn")}
                    </button>
                </div>
            </form>
        </div>

        <#if realm.registrationAllowed && registrationAllowed>
            <div id="kc-registration-container">
                <a href="${url.registrationUrl}">${msg("doRegister")}</a>
            </div>
        </#if>

        <div id="kc-info">
            <#if realm.resetPasswordAllowed>
                <a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
            </#if>
        </div>

    </div>
</div>

<script src="${url.resourcesPath}/js/script.js"></script>

</body>
</html>