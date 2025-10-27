package com.satlock.domain

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


/*

Example

{
    "id": 1,
    "name": "Leanne Graham",
    "username": "Bret",
    "email": "Sincere@april.biz",
    "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": {
    "lat": "-37.3159",
    "lng": "81.1496"
}
},
    "phone": "1-770-736-8031 x56442",
    "website": "hildegard.org",
    "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
}
}

*/
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    @Embedded(prefix = "address_")
    val address: Address,
    @Embedded(prefix = "company_")
    val company: Company,

    var isFavorite: Boolean = false,
    var pendingSync: Boolean = false
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    @Embedded(prefix = "geo_")
    val geo: Geo
)

data class Geo(
    val lat: String,
    val lng: String
)

data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)