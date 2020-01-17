package es.lifevit.sdk;


import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import es.lifevit.sdk.bracelet.LifevitSDKAlarmTime;
import es.lifevit.sdk.bracelet.LifevitSDKAppNotification;
import es.lifevit.sdk.bracelet.LifevitSDKMonitoringAlarm;
import es.lifevit.sdk.bracelet.LifevitSDKSedentaryAlarm;
import es.lifevit.sdk.utils.LogUtils;


public class BraceletAT2019SendQueue extends Thread {

    private final static String TAG = BraceletAT2019SendQueue.class.getSimpleName();

    private volatile LinkedBlockingQueue<BraceletAT2019QueueItem> sendQueue = new LinkedBlockingQueue<>();
    private boolean queueIsBusy = false;
    private LifevitSDKBleDeviceBraceletAT2019 dgBleDeviceBracelet;
    private boolean finished;
    private long timeLaunched = 0;


    public BraceletAT2019SendQueue(LifevitSDKBleDeviceBraceletAT2019 bleDevice) {
        this.dgBleDeviceBracelet = bleDevice;
        finished = false;
    }


    /**
     * Inner class
     */
    protected class BraceletAT2019QueueItem {
        public int action;
        public Object[] object;

        public BraceletAT2019QueueItem(int action, Object... object) {
            this.action = action;
            this.object = object;
        }
    }


    protected void addToQueue(int action) {
        addToQueue(action, 0);
    }

    protected void addToQueue(int action, Object... object) {
        if (!finished) {
            // Check if operation is already in queue
            boolean found = false;
           /* for(BraceletAT2019QueueItem queueItem : sendQueue){
                if(action.equals(queueItem.action) && daysAgo == queueItem.daysAgo){
                    found = true;
                    break;
                }
            }*/
            if (!found) {

                sendQueue.add(new BraceletAT2019QueueItem(action, object));

                LogUtils.log(Log.DEBUG, TAG, "Action in queue: " + action + ", " + object);
            } else {
                LogUtils.log(Log.ERROR, TAG, "Action repeated in queue: " + action + ", " + object);
            }
        }
    }


    protected void taskFinished() {
        LogUtils.log(Log.DEBUG, TAG, "Bracelet action finished.");
        queueIsBusy = false;
    }

    protected void queueFinished() {
        finished = true;
    }


    protected boolean isQueueBusy() {
        return queueIsBusy;
    }


    @Override
    public void run() {

        while (!finished) {
            try {
                if (sendQueue.size() > 0 && !finished) {
                    if (!queueIsBusy && !finished) {

                        BraceletAT2019QueueItem bqi = sendQueue.take();
                        queueIsBusy = true;
                        timeLaunched = System.currentTimeMillis();

                        LogUtils.log(Log.DEBUG, TAG, "Sending action to bracelet: " + bqi.action);

                        switch (bqi.action) {
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_BASIC_INFO:
                                dgBleDeviceBracelet.sendGetBasicInfo();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_FEATURE_LIST:
                                dgBleDeviceBracelet.sendGetFeatureList();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_TIME:
                                if (bqi.object[0] instanceof Long) {
                                    dgBleDeviceBracelet.sendSetTime((Long) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_DEVICE_TIME:
                                dgBleDeviceBracelet.sendGetDeviceTime();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNC_DATA:
                                dgBleDeviceBracelet.sendSynchronizeData();
                                break;


                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNC_SPORTS_DATA:
                                dgBleDeviceBracelet.sendSynchronizeSportsData(1);
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_SLEEP_DATA:
                                dgBleDeviceBracelet.sendSynchronizeSleepData(1);
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HEART_RATE_DATA:
                                dgBleDeviceBracelet.sendSynchronizeHeartRateData(1);
                                break;


                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_SPORT_DATA:
                                dgBleDeviceBracelet.sendSynchronizeHistoricSportData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_SLEEP_DATA:
                                dgBleDeviceBracelet.sendSynchronizeHistoricSleepData();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SYNCHRONIZE_HISTORIC_HEART_RATE_DATA:
                                dgBleDeviceBracelet.sendSynchronizeHistoricHeartRateData();
                                break;


                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_CONFIGURE_ALARM:
                                if (bqi.object[0] instanceof LifevitSDKAlarmTime) {
                                    dgBleDeviceBracelet.sendConfigureAlarm((LifevitSDKAlarmTime) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_GOALS:
                                if (bqi.object[1] instanceof LifevitSDKAlarmTime) {
                                    dgBleDeviceBracelet.sendSetGoals((int) bqi.object[0], (LifevitSDKAlarmTime) bqi.object[1]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SET_USER_INFORMATION:
                                dgBleDeviceBracelet.sendSetUserInformation((int) bqi.object[0], (int) bqi.object[1], (int) bqi.object[2], (long) bqi.object[3]);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_CONFIGURE_BRACELET_SEDENTARY_ALARM:
                                if (bqi.object[0] instanceof LifevitSDKMonitoringAlarm) {
                                    dgBleDeviceBracelet.sendConfigureBraceletSedentaryAlarm((LifevitSDKMonitoringAlarm) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ANTITHEFT:
                                if (bqi.object[0] instanceof Boolean) {
                                    dgBleDeviceBracelet.sendConfigureAntitheft((Boolean) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_RISE_HAND:
                                if (bqi.object[0] instanceof Boolean) {
                                    dgBleDeviceBracelet.sendConfigureRiseHand((Boolean) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ANDROID_PHONE:
                                dgBleDeviceBracelet.sendConfigureAndroidPhone();
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_HEART_RATE_INTERVAL_SETTING:
                                dgBleDeviceBracelet.sendConfigureHeartRateIntervalSetting((int) bqi.object[0], (int) bqi.object[1], (int) bqi.object[2], (int) bqi.object[3]);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_HEART_RATE_MONITORING:
                                if (bqi.object[2] instanceof LifevitSDKSedentaryAlarm) {
                                    dgBleDeviceBracelet.sendConfigureHeartRateMonitoring((Boolean) bqi.object[0], (Boolean) bqi.object[1], (LifevitSDKSedentaryAlarm) bqi.object[2]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_FIND_PHONE:
                                dgBleDeviceBracelet.sendConfigureFindPhone((Boolean) bqi.object[0]);
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_ACNS:
                                if (bqi.object[0] instanceof LifevitSDKAppNotification) {
                                    dgBleDeviceBracelet.sendConfigureACNS((LifevitSDKAppNotification) bqi.object[0]);
                                }
                                break;

                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_SLEEP_MONITORING:
                                if (bqi.object[1] instanceof LifevitSDKMonitoringAlarm) {
                                    dgBleDeviceBracelet.sendConfigureSleepMonitoring((Boolean) bqi.object[0], (LifevitSDKMonitoringAlarm) bqi.object[1]);
                                }
                                break;

                            //protected void sendConfigureACNSActivate() {

                                /*
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_GET_REAL_TIME_DATA:
                                if (bqi.object instanceof Boolean) {
                                    dgBleDeviceBracelet.sendGetRealTimeData((Boolean) bqi.object);
                                }
                                break;
                                 */

                                /*
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_END_SYNC_DATA:
                                dgBleDeviceBracelet.sendEndSynchronizeData();
                                break;

                                 */

                                /*
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_BIND:
                                    dgBleDeviceBracelet.sendBind();
                                break;
                            case LifevitSDKBleDeviceBraceletAT2019.ACTION_UNBIND:
                                    dgBleDeviceBracelet.sendUnbind();;
                                break;
                                */

                        }
                    } else {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - timeLaunched > 60000) {
                            LogUtils.log(Log.DEBUG, TAG, "Interrupted action because it was taking too long!");

                            queueIsBusy = false;
                        }
                        Thread.sleep(200);
                    }
                } else {
                    Thread.sleep(200);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}