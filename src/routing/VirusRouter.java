/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

public class VirusRouter extends ActiveRouter {

    private boolean hasSmallVirus = false;
    private boolean hasBigVirus = false;

    public VirusRouter(Settings s) {
        super(s);
    }

    protected VirusRouter(VirusRouter r) {
        super(r);
        this.hasSmallVirus = r.isHasSmallVirus();
        this.hasBigVirus = r.isHasBigVirus();
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
        System.out.println("Created virus: " + m);
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
            this.setHasBigVirus(true);
        } else if (((VirusRouter) from.getRouter()).isHasSmallVirus() && virusType.equals("SV")) {
            this.setHasSmallVirus(true);
        }
        return m;
    }
}
