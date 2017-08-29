package com.codepoetics.raffia

import com.codepoetics.raffia.builders.BasketWeaver

object Raffia {

    fun weaver(): BasketWeaver {
        return BasketWeaver.create()
    }

}
