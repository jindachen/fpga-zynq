package zynq

import chisel3._
import diplomacy.{LazyModule, LazyModuleImp}
import config.{Parameters, Field}
import rocketchip._
import testchipip._

case object ZynqAdapterBase extends Field[BigInt]

class Top(implicit val p: Parameters) extends Module {
  val address = p(ZynqAdapterBase)
  val config = p(ExtIn)
  val target = Module(LazyModule(new FPGAZynqTop).module)
  val adapter = Module(LazyModule(new ZynqAdapter(address, config)).module)

  require(target.mem_axi4.size == 1)

  val io = IO(new Bundle {
    val ps_axi_slave = Flipped(adapter.io.axi.head.cloneType)
    val mem_axi = target.mem_axi4.head.cloneType
  })

  io.mem_axi <> target.mem_axi4.head
  adapter.io.axi.head <> io.ps_axi_slave
  adapter.io.serial <> target.serial
  adapter.io.bdev <> target.bdev
  target.reset := adapter.io.sys_reset
}

class FPGAZynqTop(implicit p: Parameters) extends BaseSystem
    with HasPeripheryMasterAXI4MemPort
    with HasPeripheryErrorSlave
    with HasPeripheryZeroSlave
    with HasPeripheryBootROM
    with HasPeripheryRTCCounter
    with HasRocketPlexMaster
    with HasNoDebug
    with HasPeripherySerial
    with HasPeripheryBlockDevice {
  override lazy val module = new FPGAZynqTopModule(this)
}

class FPGAZynqTopModule(outer: FPGAZynqTop) extends BaseSystemModule(outer)
    with HasPeripheryMasterAXI4MemPortModuleImp
    with HasPeripheryBootROMModuleImp
    with HasPeripheryRTCCounterModuleImp
    with HasRocketPlexMasterModuleImp
    with HasNoDebugModuleImp
    with HasPeripherySerialModuleImp
    with HasPeripheryBlockDeviceModuleImp
