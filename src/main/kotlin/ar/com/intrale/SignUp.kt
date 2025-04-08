package ar.com.intrale

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.SignUpRequest
import com.google.gson.Gson
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern
import net.datafaker.Faker
import net.datafaker.providers.base.Text
import net.datafaker.providers.base.Text.DIGITS
import net.datafaker.providers.base.Text.EN_UPPERCASE
import org.slf4j.Logger

class SignUp (val config: Config, val faker: Faker, val logger: Logger): Function {

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

            val email: String = body.email

            val attributeType =
                AttributeType {
                    this.name = "email"
                    this.value = email
                }

            val attrs = mutableListOf<AttributeType>()
            attrs.add(attributeType)

            val request =
                SignUpRequest {
                    userAttributes = attrs
                    username = email
                    clientId = config.awsCognitoClientId
                    password = faker.text().text(Text.TextSymbolsBuilder.builder()
                        .len(8)
                        .with(EN_UPPERCASE, 2)
                        .with(DIGITS, 3).build())
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
            errorsMessage += it.dataPath.substring(1) + ' ' + it.message
        }

        return RequestValidationException(errorsMessage)
    }

}