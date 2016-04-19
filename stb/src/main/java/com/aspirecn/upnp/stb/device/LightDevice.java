package com.aspirecn.upnp.stb.device;

import org.cybergarage.net.HostInterface;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import java.io.InputStream;

/**
 * Created by yinghuihong on 16/4/18.
 */
public class LightDevice extends Device implements ActionListener, QueryListener {

    private StateVariable powerVar;

    public LightDevice(String path) throws InvalidDescriptionException {
        super(path);
        setSSDPBindAddress(HostInterface.getInetAddress(HostInterface.IPV4_BITMASK, null));
        setHTTPBindAddress(HostInterface.getInetAddress(HostInterface.IPV4_BITMASK, null));
        Action getPowerAction = getAction("GetPower");
        getPowerAction.setActionListener(this);

        Action setPowerAction = getAction("SetPower");
        setPowerAction.setActionListener(this);

        ServiceList serviceList = getServiceList();
        Service service = serviceList.getService(0);
        service.setQueryListener(this);

        powerVar = getStateVariable("Power");
    }

    ////////////////////////////////////////////////
    //	on/off
    ////////////////////////////////////////////////

    /**
     * @Note 标记当前状态
     */
    private boolean onFlag = false;

    public boolean isOn() {
        return onFlag;
    }

    /**
     * @param state "on" or "off"
     * @return 成功或失败
     * @Note 更改状态
     */
    public boolean setPowerState(String state) {
        if (state == null) {
            onFlag = false;
            powerVar.setValue("off");
            return false;
        }
        if (state.compareTo("on") == 0) {
            onFlag = true;
            powerVar.setValue("on");
            return true;
        }
        if (state.compareTo("off") == 0) {
            onFlag = false;
            powerVar.setValue("off");
            return true;
        }
        return false;
    }

    /**
     * @return "on" or "off"
     * @Note 获取状态
     */
    public String getPowerState() {
        if (onFlag)
            return "on";
        return "off";
    }

    ////////////////////////////////////////////////
    // ActionListener	@Note 接收到控制消息,执行操作
    ////////////////////////////////////////////////

    public boolean actionControlReceived(Action action) {
        String actionName = action.getName();

        boolean ret = false;

        if (actionName.equals("GetPower")) {
            String state = getPowerState();
            Argument powerArg = action.getArgument("Power");// @Note 返回Power状态变量
            powerArg.setValue(state);// @Note 设置该action对象的数值,再将该action对象作为响应数据
            ret = true;
        }
        if (actionName.equals("SetPower")) {
            Argument powerArg = action.getArgument("Power");
            String state = powerArg.getValue();
            boolean rst = setPowerState(state);    // @Note 设置数值,并将变更通知订阅者们,返回操作结果

//			state = getPowerState(); // @Note edit by yinghuihong
            Argument resultArg = action.getArgument("Result");// @Note 返回Result状态变量,成功或失败的数值
            resultArg.setValue(rst ? "成功" : "失败");    // @Note 取当前本地数值,作为响应数值
            ret = true;
        }

//        comp.repaint();
        repaintListener.repaint();
        return ret;
    }

    ////////////////////////////////////////////////
    // QueryListener	@Note 不同于Action,采用StateVariable
    ////////////////////////////////////////////////

    public boolean queryControlReceived(StateVariable stateVar) {
        stateVar.setValue(getPowerState());// @Note 返回当前状态
        return true;
    }

    ////////////////////////////////////////////////
    // update
    ////////////////////////////////////////////////

    public void update() {
    }

    ////////////////////////////////////////////////
    // repaint
    ////////////////////////////////////////////////

    public interface RepaintListener {
        void repaint();
    }

    private RepaintListener repaintListener;

    public void setRepaintListener(RepaintListener listener) {
        this.repaintListener = listener;
    }

}
