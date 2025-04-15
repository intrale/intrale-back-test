package ar.com.intrale

import io.ktor.http.HttpStatusCode

class UnauthorizeExeption() : Response(statusCode = HttpStatusCode.Unauthorized) {
}