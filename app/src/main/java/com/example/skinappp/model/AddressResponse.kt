package com.example.skinappp.model

data class AddressResponse(
    val features: List<Feature>,
    val query: Query,
    val type: String
)