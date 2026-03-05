return [
    interceptor()
        .constrainedBy( isMethod("GET"), pathMatches("/**/carts/current") )
        .perform( jsonResponse('{"type": "cartWsDTO", "totalItems": 5}') )
]