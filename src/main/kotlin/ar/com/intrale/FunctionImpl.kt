package ar.com.intrale

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.SignUpRequest
import com.google.gson.Gson
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class FunctionImpl : Function {


    override suspend fun execute(textBody:String): FunctionResponse {

        if (textBody.isEmpty()) return FunctionResponse("Request body not found")

        var body = Gson().fromJson(textBody, FunctionRequest::class.java)

        var validation = Validation<FunctionRequest> {
            FunctionRequest::email  {
                minLength(1) hint  "El campo email es obligatorio"
                pattern(".+@.+\\..+") hint "El campo email debe tener formato de email. Valor actual: '{value}'"
            }
        }

        var validationResult: ValidationResult<Any> = validation(body)

        if (validationResult.isValid){

            val clientIdVal: String = "1ve1nokbjnmhk1adiben0a9iao"
            val secretKey: String = "2i0k4EloPyS2aTsW+YsuxFgTE9vauyCc8bZZeljf"
            val usernameVal: String = "leolarreta"
            val passwordVal: String = "asdfasdfasfd"
            val email: String = body.email

            val attributeType =
                AttributeType {
                    this.name = "email"
                    this.value = email
                }

            val attrs = mutableListOf<AttributeType>()
            attrs.add(attributeType)
            val secretVal = calculateSecretHash(clientIdVal, secretKey, usernameVal)

            val request =
                SignUpRequest {
                    userAttributes = attrs
                    username = usernameVal
                    clientId = clientIdVal
                    password = passwordVal
                    secretHash = secretVal
                }
            CognitoIdentityProviderClient { region = "us-east-2" }.use { identityProviderClient ->
                identityProviderClient.signUp(request)
                println("User has been signed up")
            }

        }

        var errorsMessage: String = ""
        validationResult.errors.forEach {
            errorsMessage += it.message
        }

        return FunctionResponse(errorsMessage)
    }


    fun calculateSecretHash(
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
    }

}