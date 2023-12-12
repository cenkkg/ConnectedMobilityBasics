package interfaces;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
import routing.VirusRouter;

import java.util.Collection;

public class BigVirusInterface extends NetworkInterface {

    public BigVirusInterface(Settings s)	{
        super(s);
    }

    public BigVirusInterface(BigVirusInterface ni) {
        super(ni);
    }

    @Override
    public NetworkInterface replicate() {
        return new BigVirusInterface(this);
    }

    @Override
    public void connect(NetworkInterface anotherInterface) {
        if (isScanning()
                && anotherInterface.getHost().isRadioActive()
                && isWithinRange(anotherInterface)
                && !isConnected(anotherInterface)
                && (this != anotherInterface)) {
            // new contact within range
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed(this);
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);
            connect(con,anotherInterface);
        }
    }

    public void update() {
        if (optimizer == null) {
            return; /* nothing to do */
        }

        // First break the old ones
        optimizer.updateLocation(this);
        for (int i=0; i<this.connections.size(); ) {
            Connection con = this.connections.get(i);
            NetworkInterface anotherInterface = con.getOtherInterface(this);

            // all connections should be up at this stage
            assert con.isUp() : "Connection " + con + " was down!";

            if (!isWithinRange(anotherInterface)) {
                disconnect(con,anotherInterface);
                connections.remove(i);
            }
            else {
                i++;
            }
        }
        // Then find new possible connections
        Collection<NetworkInterface> interfaces =
                optimizer.getNearInterfaces(this);
        for (NetworkInterface i : interfaces) {
            VirusRouter hostRouter = (VirusRouter) getHost().getRouter();
            if (hostRouter.isHasBigVirus()) {
                connect(i);
            }
        }
    }

    @Override
    public void createConnection(NetworkInterface anotherInterface) {
        if (!isConnected(anotherInterface) && (this != anotherInterface)) {
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed(this);
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);

            System.out.println("Connected hosts: " + this.getHost().toString() + " --- " + anotherInterface.getHost().toString());
            connect(con,anotherInterface);
        }
    }

    public String toString() {
        return "BigVirusInterface " + super.toString();
    }
}
