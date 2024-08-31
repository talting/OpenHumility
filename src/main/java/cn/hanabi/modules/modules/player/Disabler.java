package cn.hanabi.modules.modules.player;

import cn.hanabi.events.EventPacket;
import cn.hanabi.events.EventPostMotion;
import cn.hanabi.events.EventPreMotion;
import cn.hanabi.modules.Category;
import cn.hanabi.modules.Mod;
import cn.hanabi.value.Value;
import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.darkmagician6.eventapi.types.EventType;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C0BPacketEntityAction.Action;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraftforge.event.world.WorldEvent;

public class Disabler extends Mod {
   private final Value mode = (new Value("Disabler", "Mode", 0)).LoadValue(new String[]{"Grim"});
   private int lastSlot;
   private boolean lastSprinting;
   private static boolean lastResult;
   public static final CopyOnWriteArrayList<Packet> storedPackets = new CopyOnWriteArrayList();
   public static final ConcurrentLinkedDeque<Integer> pingPackets = new ConcurrentLinkedDeque();
   public static Value s2ffix = new Value("Disabler", "S2FCheck", false);
   public static Value c09fix = new Value("Disabler", "C09Check", false);
   public static Value c0bfix = new Value("Disabler", "C0BCheck", false);
   public static Value c08 = new Value("Disabler", "Grim Place", false);
   public static Value c07 = new Value("Disabler", "FastBreak Mender", false);
   public static Value c07fix = new Value("Disabler", "FastBreak Mender(BadPacketZPreviewFix)", false);
   public static Value c0ffix = new Value("Disabler", "PostCheck", false);

   public Disabler() {
      super("Disabler", Category.PLAYER);
      this.setState(false);
   }

   @EventTarget
   public void onWorld(WorldEvent e) {
      this.lastSlot = -1;
      this.lastSprinting = false;
   }

   @EventTarget
   public void onUpdatePost(EventPostMotion e) {
      if (((Boolean)c0ffix.getValue()).booleanValue() && !getGrimPost()) {
         processPackets();
      }

   }

   @EventTarget
   public void onUpdatePre(EventPreMotion e) {
      if (((Boolean)c0ffix.getValue()).booleanValue() && !getGrimPost()) {
         processPackets();
      }

   }

   @EventTarget
   public void onPacket(EventPacket e) {
      if (((Boolean)c0bfix.getValueState()).booleanValue() && e.getPacket() instanceof C0BPacketEntityAction) {
         if (((C0BPacketEntityAction)e.getPacket()).getAction() == Action.START_SPRINTING) {
            if (this.lastSprinting) {
               e.setCancelled(true);
            }

            this.lastSprinting = true;
         } else if (((C0BPacketEntityAction)e.getPacket()).getAction() == Action.STOP_SPRINTING) {
            if (!this.lastSprinting) {
               e.setCancelled(true);
            }

            this.lastSprinting = false;
         }
      }

      if (((Boolean)c09fix.getValueState()).booleanValue() && e.getPacket() instanceof C09PacketHeldItemChange) {
         int slot = ((C09PacketHeldItemChange)e.getPacket()).getSlotId();
         if (slot == this.lastSlot && slot != -1) {
            e.setCancelled(true);
         }

         this.lastSlot = ((C09PacketHeldItemChange)e.getPacket()).getSlotId();
      }

      if (((Boolean)c07fix.getValueState()).booleanValue() && e.getPacket() instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging)e.getPacket()).getStatus() == net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
         mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, ((C07PacketPlayerDigging)e.getPacket()).getPosition(), ((C07PacketPlayerDigging)e.getPacket()).getFacing()));
      }

      if (((Boolean)c07.getValueState()).booleanValue() && e.getPacket() instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging)e.getPacket()).getStatus() == net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
         mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, ((C07PacketPlayerDigging)e.getPacket()).getPosition().add(0, 500, 0), ((C07PacketPlayerDigging)e.getPacket()).getFacing()));
      }

      if (((Boolean)c08.getValueState()).booleanValue() && e.getPacket() instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockDirection() <= 5 && ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockDirection() >= 0) {
         e.setCancelled(true);
         mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(((C08PacketPlayerBlockPlacement)e.getPacket()).getPosition(), 6 + ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockDirection() * 7, ((C08PacketPlayerBlockPlacement)e.getPacket()).getStack(), ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockOffsetX(), ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockOffsetY(), ((C08PacketPlayerBlockPlacement)e.getPacket()).getPlacedBlockOffsetZ()));
      }

      if (((Boolean)s2ffix.getValueState()).booleanValue() && ((S2FPacketSetSlot)e.getPacket()).func_149174_e().getItem() instanceof ItemSword && ((S2FPacketSetSlot)e.getPacket()).func_149173_d() == mc.thePlayer.inventory.currentItem + 36 && mc.thePlayer.isBlocking()) {
         e.setCancelled(true);
      }

   }

   public static boolean getGrimPost() {
      boolean result = mc.thePlayer != null && mc.thePlayer.isEntityAlive() && mc.thePlayer.ticksExisted >= 5 && !(mc.currentScreen instanceof GuiDownloadTerrain);
      if (lastResult && !result) {
         lastResult = false;
         mc.addScheduledTask(() -> {
            processPackets();
         });
      }

      lastResult = result;
      return result;
   }

   public static void processPackets() {
      if (!storedPackets.isEmpty()) {
         for(Packet packet : storedPackets) {
            EventPacket event = new EventPacket(EventType.RECIEVE, packet);
            EventManager.call(event);
            if (!event.isCancelled()) {
               packet.processPacket(mc.getNetHandler());
            }
         }

         storedPackets.clear();
      }

   }

   public static boolean grimPostDelay(Packet packet) {
      if (mc.thePlayer == null) {
         return false;
      } else if (mc.currentScreen instanceof GuiDownloadTerrain) {
         return false;
      } else if (packet instanceof S00PacketServerInfo) {
         return false;
      } else if (packet instanceof S01PacketEncryptionRequest) {
         return false;
      } else if (packet instanceof S38PacketPlayerListItem) {
         return false;
      } else if (packet instanceof S00PacketDisconnect) {
         return false;
      } else if (packet instanceof S40PacketDisconnect) {
         return false;
      } else if (packet instanceof S21PacketChunkData) {
         return false;
      } else if (packet instanceof S01PacketPong) {
         return false;
      } else if (packet instanceof S44PacketWorldBorder) {
         return false;
      } else if (packet instanceof S01PacketJoinGame) {
         return false;
      } else if (packet instanceof S19PacketEntityHeadLook) {
         return false;
      } else if (packet instanceof S3EPacketTeams) {
         return false;
      } else if (packet instanceof S02PacketChat) {
         return false;
      } else if (packet instanceof S2FPacketSetSlot) {
         return false;
      } else if (packet instanceof S1CPacketEntityMetadata) {
         return false;
      } else if (packet instanceof S20PacketEntityProperties) {
         return false;
      } else if (packet instanceof S35PacketUpdateTileEntity) {
         return false;
      } else if (packet instanceof S03PacketTimeUpdate) {
         return false;
      } else if (packet instanceof S47PacketPlayerListHeaderFooter) {
         return false;
      } else if (packet instanceof S12PacketEntityVelocity) {
         S12PacketEntityVelocity sPacketEntityVelocity = (S12PacketEntityVelocity)packet;
         return sPacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId();
      } else {
         return packet instanceof S27PacketExplosion || packet instanceof S32PacketConfirmTransaction || packet instanceof S08PacketPlayerPosLook || packet instanceof S18PacketEntityTeleport || packet instanceof S19PacketEntityStatus || packet instanceof S04PacketEntityEquipment || packet instanceof S23PacketBlockChange || packet instanceof S22PacketMultiBlockChange || packet instanceof S13PacketDestroyEntities || packet instanceof S00PacketKeepAlive || packet instanceof S06PacketUpdateHealth || packet instanceof S14PacketEntity || packet instanceof S0FPacketSpawnMob || packet instanceof S2DPacketOpenWindow || packet instanceof S30PacketWindowItems || packet instanceof S3FPacketCustomPayload || packet instanceof S2EPacketCloseWindow;
      }
   }

   public static void fixC0F(C0FPacketConfirmTransaction packet) {
      int id = packet.getUid();
      if (id < 0 && !pingPackets.isEmpty()) {
         while(true) {
            int current = ((Integer)pingPackets.getFirst()).intValue();
            mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(packet.getWindowId(), (short)current, true));
            pingPackets.pollFirst();
            if (current == id || pingPackets.isEmpty()) {
               break;
            }
         }
      } else {
         mc.getNetHandler().getNetworkManager().sendPacket(packet);
      }

   }
}
