package cn.hanabi.utils.jprocess.wmi4java;

import java.util.List;

interface WMIStub {
   String listClasses(String var1, String var2) throws WMIException;

   String listObject(String var1, String var2, String var3) throws WMIException;
   String queryObject(String wmiClass, List<String> wmiProperties, List<String> conditions, String namespace, String computerName) throws WMIException;


   String listProperties(String var1, String var2, String var3) throws WMIException;
}
