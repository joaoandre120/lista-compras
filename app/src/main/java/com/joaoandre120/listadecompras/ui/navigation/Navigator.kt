package com.joaoandre120.listadecompras.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Trata eventos de navegação (avançar e voltar) atualizando o [NavigationState].
 *
 * @property state Estado de navegação a ser modificado.
 */
class Navigator(val state: NavigationState) {

    /**
     * Navega para [route]: se for uma aba, apenas a seleciona (mantendo a pilha dela);
     * caso contrário, empilha na pilha da aba atual.
     *
     * @param route Rota de destino.
     */
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    /**
     * Volta uma tela. Na base de uma aba que não é a inicial, volta para a aba inicial.
     */
    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}