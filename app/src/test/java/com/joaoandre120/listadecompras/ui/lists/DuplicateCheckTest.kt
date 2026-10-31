package com.joaoandre120.listadecompras.ui.lists

import com.joaoandre120.listadecompras.data.model.CatalogProduct
import com.joaoandre120.listadecompras.data.model.ShoppingItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateCheckTest {

    private val arroz = CatalogProduct("arroz", "Arroz", "Mercearia")

    @Test
    fun `item com mesmo productId conta mesmo renomeado`() {
        val items = listOf(ShoppingItem(id = "1", name = "Arroz integral Camil", productId = "arroz"))
        assertTrue(isProductInList(arroz, items))
    }

    @Test
    fun `item digitado a mao compara pelo nome sem acento e caixa`() {
        val items = listOf(ShoppingItem(id = "1", name = "ARROZ"))
        assertTrue(isProductInList(arroz, items))
    }

    @Test
    fun `produto diferente nao e duplicata`() {
        val items = listOf(ShoppingItem(id = "1", name = "Feijão", productId = "feijao"))
        assertFalse(isProductInList(arroz, items))
    }

    @Test
    fun `texto em branco nunca e duplicata`() {
        assertFalse(isNameInList("  ", listOf(ShoppingItem(id = "1", name = "Arroz"))))
    }
}