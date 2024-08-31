package cn.hanabi.injection.mixins;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({C08PacketPlayerBlockPlacement.class})
public abstract class MixinC08PacketPlayerBlockPlacement {
   @Shadow
   private BlockPos position;
   @Shadow
   private int placedBlockDirection;
   @Shadow
   public ItemStack stack;
   @Shadow
   public float facingX;
   @Shadow
   public float facingY;
   @Shadow
   public float facingZ;

   public MixinC08PacketPlayerBlockPlacement() {
      super();
   }

   @Overwrite
   public void readPacketData(PacketBuffer p_readPacketData_1_) throws IOException {
      this.position = p_readPacketData_1_.readBlockPos();
      this.placedBlockDirection = p_readPacketData_1_.readUnsignedByte();
      this.stack = p_readPacketData_1_.readItemStackFromBuffer();
      this.facingX = (float)p_readPacketData_1_.readUnsignedByte();
      this.facingY = (float)p_readPacketData_1_.readUnsignedByte();
      this.facingZ = (float)p_readPacketData_1_.readUnsignedByte();
   }

   @Overwrite
   public void writePacketData(PacketBuffer p_writePacketData_1_) throws IOException {
      p_writePacketData_1_.writeBlockPos(this.position);
      p_writePacketData_1_.writeByte(this.placedBlockDirection);
      p_writePacketData_1_.writeItemStackToBuffer(this.stack);
      p_writePacketData_1_.writeByte((int)this.facingX);
      p_writePacketData_1_.writeByte((int)this.facingY);
      p_writePacketData_1_.writeByte((int)this.facingZ);
   }
}
