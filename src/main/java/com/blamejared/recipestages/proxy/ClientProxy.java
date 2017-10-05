package com.blamejared.recipestages.proxy;

import com.blamejared.recipestages.events.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy {
    
    
    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }
    
    @Override
    public void registerEvents() {
        super.registerEvents();
        new ClientEventHandler();
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
}
