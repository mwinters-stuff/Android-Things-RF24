package com.example.androidthings.rf24.rf24;

/**
 * Created by mathew on 20/01/17.
 * Copyright 2017 Mathew Winters
 */

@SuppressWarnings("unused")
public enum nRF24L01 {
  CONFIG(0x00),
  EN_AA(0x01),
  EN_RXADDR(0x02),
  SETUP_AW(0x03),
  SETUP_RETR(0x04),
  RF_CH(0x05),
  RF_SETUP(0x06),
  NRF_STATUS(0x07),
  OBSERVE_TX(0x08),
  CD(0x09),
  RX_ADDR_P0(0x0A),
  RX_ADDR_P1(0x0B),
  RX_ADDR_P2(0x0C),
  RX_ADDR_P3(0x0D),
  RX_ADDR_P4(0x0E),
  RX_ADDR_P5(0x0F),
  TX_ADDR(0x10),
  RX_PW_P0(0x11),
  RX_PW_P1(0x12),
  RX_PW_P2(0x13),
  RX_PW_P3(0x14),
  RX_PW_P4(0x15),
  RX_PW_P5(0x16),
  FIFO_STATUS(0x17),
  DYNPD(0x1C),
  FEATURE(0x1D),

  /* Bit Mnemonics */
  MASK_RX_DR(6),
  MASK_TX_DS(5),
  MASK_MAX_RT(4),
  EN_CRC(3),
  CRCO(2),
  PWR_UP(1),
  PRIM_RX(0),
  ENAA_P5(5),
  ENAA_P4(4),
  ENAA_P3(3),
  ENAA_P2(2),
  ENAA_P1(1),
  ENAA_P0(0),
  ERX_P5(5),
  ERX_P4(4),
  ERX_P3(3),
  ERX_P2(2),
  ERX_P1(1),
  ERX_P0(0),
  AW(0),
  ARD(4),
  ARC(0),
  PLL_LOCK(4),
  RF_DR(3),
  RF_PWR(6),
  RX_DR(6),
  TX_DS(5),
  MAX_RT(4),
  RX_P_NO(1),
  TX_FULL(0),
  PLOS_CNT(4),
  ARC_CNT(0),
  TX_REUSE(6),
  FIFO_FULL(5),
  TX_EMPTY(4),
  RX_FULL(1),
  RX_EMPTY(0),
  DPL_P5(5),
  DPL_P4(4),
  DPL_P3(3),
  DPL_P2(2),
  DPL_P1(1),
  DPL_P0(0),
  EN_DPL(2),
  EN_ACK_PAY(1),
  EN_DYN_ACK(0),

  /* Instruction Mnemonics */
  R_REGISTER(0x00),
  W_REGISTER(0x20),
  REGISTER_MASK(0x1F),
  ACTIVATE(0x50),
  R_RX_PL_WID(0x60),
  R_RX_PAYLOAD(0x61),
  W_TX_PAYLOAD(0xA0),
  W_ACK_PAYLOAD(0xA8),
  FLUSH_TX(0xE1),
  FLUSH_RX(0xE2),
  REUSE_TX_PL(0xE3),
  NOP(0xFF),

  /* Non-P omissions */
  LNA_HCURR(0),

  /* P model memory Map */
  RPD(0x09),
  W_TX_PAYLOAD_NO_ACK(0xB0),

  /* P model bit Mnemonics */
  RF_DR_LOW(5),
  RF_DR_HIGH(3),
  RF_PWR_LOW(1),
  RF_PWR_HIGH(2);

  private final byte i;

  nRF24L01(int i) {
    this.i = (byte) i;
  }

  public byte i() {
    return i;
  }
}
