<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Trading App</title>
</head>
<body>

    <h1>Iniciar sesión</h1>

    <form id="kc-form-login" action="${url.loginAction}" method="post">

        <label>Usuario</label>
        <input type="text" name="username" value="${(login.username!'')}" autofocus/>

        <label>Contraseña</label>
        <input type="password" name="password"/>

        <#if messagesPerField.existsError('username','password')>
            <p style="color:red;">
                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
            </p>
        </#if>

        <button type="submit">Entrar</button>

    </form>

</body>
</html>