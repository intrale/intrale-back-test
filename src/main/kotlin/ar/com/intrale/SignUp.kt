package ar.com.intrale

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.SignUpRequest
import com.google.gson.Gson
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern

class SignUp (val config: Config): Function {

    override suspend fun execute(textBody:String): Response {

        if (textBody.isEmpty()) return RequestValidationException("Request body not found")

        var body = Gson().fromJson(textBody, ar.com.intrale.SignUpRequest::class.java)

        var validation = Validation<ar.com.intrale.SignUpRequest> {
            ar.com.intrale.SignUpRequest::email  {
                minLength(1) hint  "El campo email es obligatorio"
                pattern(".+@.+\\..+") hint "El campo email debe tener formato de email. Valor actual: '{value}'"
            }
        }

        var validationResult: ValidationResult<Any>
        try {
            validationResult = validation(body)
        } catch (e:Exception){
            return RequestValidationException("Request is empty")
        }

        if (validationResult.isValid){

            //val clientIdVal: String = "11pm8ug3bletqjvdl4omvig43u"
            //val secretKey: String = "2i0k4EloPyS2aTsW+YsuxFgTE9vauyCc8bZZeljf"
            //val usernameVal: String = "usuario1"
            val passwordVal: String = "Prueba#1"
            val email: String = body.email

            val attributeType =
                AttributeType {
                    this.name = "email"
                    this.value = email
                }

            val attrs = mutableListOf<AttributeType>()
            attrs.add(attributeType)
            //val secretVal = calculateSecretHash(clientIdVal, secretKey, /*usernameVal*/ email)

            val request =
                SignUpRequest {
                    userAttributes = attrs
                    username = email
                    clientId = /*clientIdVal*/ config.awsCognitoClientId
                    password = passwordVal
                    //secretHash = secretVal
                }

            try {
                CognitoIdentityProviderClient {
                    region = config.region
                    credentialsProvider
                }.use { identityProviderClient ->
                    identityProviderClient.signUp(request)
                    println("User has been signed up")
                }
            } catch (e:Exception) {
                return ExceptionResponse(e.message ?: "Internal Server Error")
            }

            return Response()
        }

        var errorsMessage: String = ""
        validationResult.errors.forEach {
            errorsMessage += ' ' + it.message
        }

        return RequestValidationException(errorsMessage)
    }


   /* fun calculateSecretHash(
        userPoolClientId: String,
        userPoolClientSecret: String,
        userName: String,
    ): String {
        val macSha256Algorithm = "HmacSHA256"
        val signingKey =
            SecretKeySpec(
                userPoolClientSecret.toByteArray(StandardCharsets.UTF_8),
                macSha256Algorithm,
            )
        try {
            val mac = Mac.getInstance(macSha256Algorithm)
            mac.init(signingKey)
            mac.update(userName.toByteArray(StandardCharsets.UTF_8))
            val rawHmac = mac.doFinal(userPoolClientId.toByteArray(StandardCharsets.UTF_8))
            return Base64.getEncoder().encodeToString(rawHmac)
        } catch (e: UnsupportedEncodingException) {
            println(e.message)
        }
        return ""
    } */

}