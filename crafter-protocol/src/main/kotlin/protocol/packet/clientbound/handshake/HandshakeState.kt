package protocol.packet.clientbound.handshake

enum class HandshakeState(val type: Int) {
    State(1),
    Login(2),
    Transfer(3);
}