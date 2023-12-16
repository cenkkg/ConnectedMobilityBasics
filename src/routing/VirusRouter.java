/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;
import java.util.Random;

public class VirusRouter extends ActiveRouter {
    public static final String WC_SETTING = "wc";
    public static final boolean WC_DEFAULT = false;
    private boolean hasSmallVirus = false;
    private boolean hasBigVirus = false;
    private boolean wc = false;

    public VirusRouter(Settings s) {
        super(s);
        this.wc = s.getBoolean(WC_SETTING, WC_DEFAULT);
    }

    protected VirusRouter(VirusRouter r) {
        super(r);
        this.hasSmallVirus = r.isHasSmallVirus();
        this.hasBigVirus = r.isHasBigVirus();
        this.wc = r.isWc();
    }

    public boolean isHasSmallVirus() {
        return hasSmallVirus;
    }

    public void setHasSmallVirus(boolean hasSmallVirus) {
        this.hasSmallVirus = hasSmallVirus;
    }

    public boolean isHasBigVirus() {
        return hasBigVirus;
    }

    public void setHasBigVirus(boolean hasBigVirus) {
        this.hasBigVirus = hasBigVirus;
    }

    public boolean isWc() {
        return wc;
    }

    public void setWc(boolean wc) {
        this.wc = wc;
    }

    @Override
    public void update() {
        super.update();
//		if (isTransferring() || !canStartTransfer()) {
//			return; // transferring, don't try other connections yet
//		}
//
//		// Try first the messages that can be delivered to final recipient
//		if (exchangeDeliverableMessages() != null) {
//			return; // started a transfer, don't try others (yet)
//		}

        // then try any/all message to any/all connection
        this.tryAllMessagesToAllConnections();
    }

    @Override
    public VirusRouter replicate() {
        return new VirusRouter(this);
    }

    @Override
    public boolean createNewMessage(Message m) {
        String virusType = m.toString().substring(0, Math.min(m.toString().length(), 2));

        if(virusType.equals("LV")){
            this.setHasBigVirus(true);
            return super.createNewMessage(m);
        } else if (virusType.equals("SV")) {
            this.setHasSmallVirus(true);
            return super.createNewMessage(m);
        } else {
            return false;
        }
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message m = super.messageTransferred(id, from);

        String virusType = m.toString().substring(0, Math.min(m.toString().length(), 2));
        if (((VirusRouter) from.getRouter()).isHasBigVirus() && virusType.equals("LV")) {
            float virusInfectedProbability = 100.0F;

            if (this.isHasBigVirus()){
                return m;
            }

            if(this.isHasSmallVirus()) {
                virusInfectedProbability = virusInfectedProbability * 0.7F;
            } else {
                virusInfectedProbability = virusInfectedProbability * 0.5F;
            }

            Random random = new Random();
            float infectProbability = 0.0F + random.nextFloat() * (virusInfectedProbability);
            if(((VirusRouter) from.getRouter()).isWc()){
                infectProbability += 20.F;
                System.out.println("We are : " +  this.getHost().toString() + "From host: " + from.toString() + " infectProbability: " + infectProbability);
            }

            if(infectProbability > 30.0F){
                this.setHasBigVirus(true);
            }
        } else if (((VirusRouter) from.getRouter()).isHasSmallVirus() && virusType.equals("SV")) {
            this.setHasSmallVirus(true);
        }
        return m;
    }
}
