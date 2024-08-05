package com.crafter.structure.minecraft.protocol.packet.handshake

enum class HandshakeState(val type: Int) {
    State(1),
    Login(2),
    Transfer(3);
}