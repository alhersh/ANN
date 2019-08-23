/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BP;

import javax.swing.*;
import java.sql.*;
public class BP {
    int n=8;
    int P=0;
    int u=5;
   
    int hiddenNodes[]=null;
    double hiddenNodesValues[][];
    double outNodesValues[][]= new double [u][2];
    int epoch =0;
    double wArrF[][][]= new double [99][99][99];
    double wDeltaB[][]= new double [99][99];
    double vDeltaB[][]= new double [99][99];
    double deltaJ[][];
    double deltaK[]= new double [u];
    double deltaV[][];
    //double deltaW[]= new double [u];
    double theta=0.1;
    double alpha=1;
    public double bias=0;
    int recNo=0;
    int trainRec=0,testRec=0;
    String url = "jdbc:odbc:Neural";
	Connection con;
	Statement stmtTrain,stmtTest,stmtEx,stmtH2H,stmtH2OUT,stmtIN2H,stmtH;
    public BP(){

        try {
            con = DriverManager.getConnection(url, "myLogin", "myPassword");
		    stmtTrain = con.createStatement();
            
		    String queryTrain = "SELECT * FROM Train";
			ResultSet rsTrain = stmtTrain.executeQuery(queryTrain);
            stmtTest = con.createStatement();
		    String queryTest = "SELECT * FROM Test";
			ResultSet rsTest = stmtTest.executeQuery(queryTest);
            stmtEx = con.createStatement();
             while (rsTrain.next()){
                 trainRec +=1;
             }//while
             //rs1.close();
            rsTrain.close();
            while (rsTest.next()){
                 testRec +=1;
             }//while
            rsTest.close();
            System.out.println("Train Records : "+trainRec+" , Test Records : "+testRec);


            
        
        //JOptionPane.showMessageDialog(null, "Input Units, Hidden Layers, Hidden Units and Output Units must be < 99");
        //n=Integer.parseInt(JOptionPane.showInputDialog("Enter the number of input units",null));
        //u=Integer.parseInt(JOptionPane.showInputDialog("Enter the number of output units",null));
        //recNo=Integer.parseInt(JOptionPane.showInputDialog("Enter the number of Records",null));
        //epoch=Integer.parseInt(JOptionPane.showInputDialog("Enter the epoch number",null));
        P = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of hidden Layers",null));
        theta = Double.parseDouble(JOptionPane.showInputDialog("Enter value of Theta 0<Theta<1",null));
        alpha = Double.parseDouble(JOptionPane.showInputDialog("Enter value of Alpha",null));
        //inArr = new int [recNo][n];
        hiddenNodes =  new int [P];

        if(P==0){
          
        }
        else{
            int tempHN;
            for (int i =0 ; i < P ; i++){
                tempHN = Integer.parseInt(JOptionPane.showInputDialog("Enter Number of Nodes in Hidden Layer No. "+(i+1)));
                hiddenNodes[i] = tempHN;
                stmtEx.executeUpdate("INSERT INTO hidden(h_layer_no,node_no) VALUES ("+(i)+","+tempHN+")");


            }
            hiddenNodesValues = new double [hiddenNodes[0]][2];
            deltaJ = new double [hiddenNodes[0]][2];
            deltaV = new double [hiddenNodes[0]*n][2];
            //hiddenNodesValues = new double [P][2];
            for (int i =0 ; i<P;i++){
                for (int j =0 ; j< hiddenNodes[i];j++){
                    hiddenNodesValues[i][j]=0;
                    //System.out.println("bobobob");
                }
            }

            //wArrIn =  new double[recNo*n][P];
            //wArrP =  new double[n*P][P];
        }
        con.close();
        } catch (Exception e) {
        }
        //wArrIn=initWArr(wArrIn);
        wArrF = initWArr(wArrF);
        //wArrP = wArrIn;
    }//constructor
    public double[][][] initWArr(double arr[][][]){
//        System.out.println("Inside initWArr");
        int counter= 0;
        int cntr=0;
        try{
            con = DriverManager.getConnection(url, "myLogin", "myPassword");
            stmtEx = con.createStatement();

        do{
            if(counter == 0 && P!=0){
                for (int i =0 ; i< n ; i ++){
                    for (int j=0 ; j < hiddenNodes[0];j++){
                        arr[counter][i][j] = wRndGen();
                        System.out.println("In2H In Layer "+counter +" : "+i+","+j+" : value = "+arr[counter][i][j]);

                            stmtEx.executeUpdate("INSERT INTO IN2H(in_node,out_node,valueW) VALUES ("+(i)+","+(j)+","+arr[counter][i][j]+")");

                    }
                }// input units
                //counter ++;
            }//if 0 ==> from input to hidden
            else if (counter == P && P!=0){
                for (int i =0 ; i< hiddenNodes[hiddenNodes.length -1] ; i ++){
                    for (int j=0 ; j < u ;j++){
                        arr[counter][i][j] = wRndGen();
                        System.out.println("H2Out In Layer "+counter+" : "+i+","+j+" : value = "+arr[counter][i][j]);
                        stmtEx.executeUpdate("INSERT INTO H2OUT(in_node,out_node,valueW) VALUES ("+(i)+","+(j)+","+arr[counter][i][j]+")");
                    }
                }// input units
               // counter++;
            }// from hidden to output layer
            else{

                do{
                    if(hiddenNodes.length==0){
                        break;
                    }
                    else if (cntr < hiddenNodes.length-1)
                    for (int i =0 ; i< hiddenNodes[cntr]; i++){
                        for(int j =0 ; j<hiddenNodes[cntr+1];j++){
                            arr[counter+cntr][i][j] = wRndGen();
                            System.out.println("H2H In Hidden Layer "+(counter+cntr)+" : "+i+","+j+" : value = "+arr[counter+cntr][i][j]);
                            stmtEx.executeUpdate("INSERT INTO H2H(h_layer_no,in_node,out_node,valueW) VALUES ("+(counter+cntr)+","+(i)+","+(j)+","+arr[counter+cntr][i][j]+")");
                        }

                    }
                    cntr++;
        //            System.out.println("Inside cntr "+cntr);
                }while(cntr< hiddenNodes.length-1);
            }
            counter++;
          //  System.out.println("Inside Counter "+counter);
        }while (counter<=P);
        System.out.println("---------------------------------End Initialization Weigths");
        con.close();
        }catch (Exception s){System.out.println("Err"+ s.getMessage());}
        return arr;

    }//initialize array

    public void lastW(){
        int counter= 0;
        int cntr=0;

        do{
            if(counter == 0 && P!=0){
                for (int i =0 ; i< n ; i ++){
                    for (int j=0 ; j < hiddenNodes[0];j++){

                        System.out.println("In2H In Layer "+counter +" : "+i+","+j+" : value = "+wArrF[counter][i][j]);



                    }
                }// input units
                //counter ++;
            }//if 0 ==> from input to hidden
            else if (counter == P && P!=0){
                for (int i =0 ; i< hiddenNodes[hiddenNodes.length -1] ; i ++){
                    for (int j=0 ; j < u ;j++){

                        System.out.println("H2Out In Layer "+counter+" : "+i+","+j+" : value = "+wArrF[counter][i][j]);

                    }
                }// input units
               // counter++;
            }// from hidden to output layer
            else{

                do{
                    if(hiddenNodes.length==0){
                        break;
                    }
                    else if (cntr < hiddenNodes.length-1)
                    for (int i =0 ; i< hiddenNodes[cntr]; i++){
                        for(int j =0 ; j<hiddenNodes[cntr+1];j++){

                            System.out.println("H2H In Hidden Layer "+(counter+cntr)+" : "+i+","+j+" : value = "+wArrF[counter+cntr][i][j]);

                        }

                    }
                    cntr++;
        //            System.out.println("Inside cntr "+cntr);
                }while(cntr< hiddenNodes.length-1);
            }
            counter++;
          //  System.out.println("Inside Counter "+counter);
        }while (counter<=P);
        System.out.println("---------------------------------LAST Weigths");

    }


    public void trainF(){
        double t=0;

        //do{
        try {
            Connection conT,conIN2H,conH2OUT;//,conH,conH2H,conH2OUT;
            conT = DriverManager.getConnection(url, "myLogin", "myPassword");
            //conH = DriverManager.getConnection(url, "myLogin", "myPassword");
            //conH2H = DriverManager.getConnection(url, "myLogin", "myPassword");
            conH2OUT = DriverManager.getConnection(url, "myLogin", "myPassword");
            conIN2H = DriverManager.getConnection(url, "myLogin", "myPassword");
		    stmtTrain = conT.createStatement();
            //stmtH = conH.createStatement();
            //stmtH2H = conH2H.createStatement();
            stmtH2OUT = conH2OUT.createStatement();
            stmtIN2H = conIN2H.createStatement();
		    String queryTrain = "SELECT * FROM Train";
            //String queryH = "SELECT * FROM hidden";
            //String queryH2H = "SELECT * FROM H2H";
            String queryH2OUT = "SELECT * FROM H2OUT";
            String queryIN2H = "SELECT * FROM IN2H";
            //String queryEx = "SELECT VALUEW FROM ";
			ResultSet rsTrain = stmtTrain.executeQuery(queryTrain);
          //  ResultSet rsH = stmtH.executeQuery(queryH);
            //ResultSet rsH2H = stmtH2H.executeQuery(queryH2H);
            
            
            
            int in_node,out_node,cntrP=1;
            double parents,has_nurs,form,children,housing,finance,social,health,value;
            double [] arrX = new double[n];
            int out1,out2,ou3,out4,out5;
            //System.out.println("Inside TrainF");
            double sumIn2H=0,n0=0,n1=0,n2=0,n3=0,n4=0,n5=0,n6=0,n7=0;
            double [] sumArrIn2H = new double [n];
            //double [][] sumArrH2H = new double [(P+1)*99][2];
            //double [] sumArrH2Out = new double [u];
            //double sumH2H=0;
            //double sumH2OUT=0;
            //int cntrIN2H,hF,hL;
            for (int i =0 ; i < sumArrIn2H.length;i++){
                sumArrIn2H[i]=0;
            }
            while (rsTrain.next()) {
                
                parents = rsTrain.getDouble("parents");
                arrX[0] = parents;
                //System.out.println("Parents " + parents);
                has_nurs = rsTrain.getDouble("has_nurs");
                arrX[1] = has_nurs;
                //System.out.println("has_nurs "+ has_nurs);
                form = rsTrain.getDouble("form");
                arrX[2] = form;
                //System.out.println("form "+form);
                children = rsTrain.getDouble("children");
                arrX[3] = children;
                housing = rsTrain.getDouble("housing");
                arrX[4] = housing;
                finance = rsTrain.getDouble("finance");
                arrX[5] = finance;
                social = rsTrain.getDouble("social");
                arrX[6] = social;
                health = rsTrain.getDouble("health");
                arrX[7] = health;
                t = rsTrain.getDouble("output_class");
                //System.out.println(cntrP+" --> t = "+t);
                /*
                System.out.println("parents value "+parents);
                System.out.println("has_nurs value "+has_nurs);
                System.out.println("form value "+form);
                System.out.println("children value "+children);
                System.out.println("housing value "+housing);
                System.out.println("finance value "+finance);
                System.out.println("social value "+social);
                System.out.println("health value "+health);
                */
                for (int i = 0 ; i <hiddenNodes[0];i++){
                    ResultSet rsIN2H = stmtIN2H.executeQuery(queryIN2H);
                    //System.out.println("inside hidden Node "+hiddenNodes[0]);
                   // Have to add more loop for the number of inputs
                    while (rsIN2H.next()){

                        out_node = rsIN2H.getInt("out_node");
                        //System.out.println("out node " + out_node);
                        in_node = rsIN2H.getInt("in_node");
                        //System.out.println("out node " + in_node);
                        value = rsIN2H.getDouble("valueW");
                        //System.out.println("Value W " + value);
                       //for (int inNode=0; inNode<n;inNode++){
                        if (out_node == i){
       
                            if (in_node==0)
                            {
                                n0 = parents*value;
                            }
                            if (in_node==1)
                            {
                                n1 = has_nurs*value;
                            }
                            if (in_node==2)
                            {
                                n2 = form*value;
                            }
                            if (in_node==3)
                            {
                                n3 = children*value;
                            }
                            if (in_node==4)
                            {
                                n4 = housing*value;
                            }
                            if (in_node==5)
                            {
                                n5 = finance*value;
                            }
                            if (in_node==6)
                            {
                                n6 = social*value;
                            }
                            if (in_node==7)
                            {
                                n7 = health*value;
                            }
                             
                            
                            }//for inNode
                            
                       // }//if i the nodes in the first hidden layer
                       
                    }//while rsIN2H
                    sumIn2H = n0+n1+n2+n3+n4+n5+n6+n7;
                    //System.out.println("Sum Before Activation "+sumIn2H);
                    //System.out.println("SumIn2H : = "+i+" ==> "+ activationFunction(sumIn2H));
                    //hiddenNodesValues[i][0]=0;//sumIn2H;
                    hiddenNodesValues[i][1] = sumIn2H;
                    hiddenNodesValues[i][0]= activationFunction(sumIn2H);
                    sumIn2H=0;
                   // sumArrIn2H= null;
                    sumIn2H=0;n0=0;n1=0;n2=0;n3=0;n4=0;n5=0;n6=0;n7=0;
                    rsIN2H.close();
                    
                }// for the first hidden layer


              //  for (int i =0 ; i < hiddenNodesValues.length;i++){
              //      System.out.println("Out From Hidden Node "+i+" = "+hiddenNodesValues[i][0]);
               // }
                // Moving from the didden layer to the output layer ....
                ///////////////////////////////////////////////////////////
                
                for (int i=0;i<u;i++){
                    //System.out.println("inside h->out");
                    ResultSet rsH2OUT = stmtH2OUT.executeQuery(queryH2OUT);

                    while(rsH2OUT.next()){
                    out_node = rsH2OUT.getInt("out_node");
                    //System.out.println("out node " + out_node);
                    in_node = rsH2OUT.getInt("in_node");
                    //System.out.println("in node " + in_node);
                    value = rsH2OUT.getDouble("valueW");
                    //System.out.println("Value W " + value);
                    if (out_node == i){
                        for (int x=0; x< hiddenNodesValues.length ;x++){
                            if (in_node == x){
                                outNodesValues[i][0]+= hiddenNodesValues[in_node][0]*value;
                            }
                        }

                    }//if i the nodes in the first hidden layer
                }// while rsH2OUT values
                rsH2OUT.close();
                }// The number of output nodes

                for (int i =0 ; i < outNodesValues.length;i++){
                    outNodesValues[i][1]= activationFunction(outNodesValues[i][0]);
                //    System.out.println("Out Node "+i+" = "+outNodesValues[i][1]);
                }
                calcDeltaK(outNodesValues, t);
                calcDeltaWeight();
                calcDeltaJ();
                calcDeltaV(arrX);
                adjWB();

               // System.out.println("---------------------------------------End Pattern: "+cntrP);
                sqrdErr(t,outNodesValues);
 
                cntrP++;
                

            }//while rs
         conT.close();
         //conH.close();
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
        }
        epoch++;
        
        //}while(sqrdErr(t,outNodesValues)>theta);
        System.out.println("--------------------------END EPOCH : "+epoch);
        lastW();
    }//trainF

    public void calcDeltaV(double [] arrX){
         //System.out.println("Inside calcDeltaJ");

        for (int i=0; i < deltaJ.length; i++){
            for (int j = 0 ; j< arrX.length;j++){
                vDeltaB[i][j]= alpha*deltaJ[i][0]*arrX[j];
                //System.out.println("Delta V : ("+i+","+j+") : "+vDeltaB[i][j]);
            }// for the number of hidden Nodes Values
        }// for the number of deltaK

    }//calulate delta V

    public void calcDeltaJ(){
        //System.out.println("Inside calcDeltaJ");
        try {
            Connection conH2OUT;//,conH,conH2H,conH2OUT;
            conH2OUT = DriverManager.getConnection(url, "myLogin", "myPassword");
            stmtH2OUT = conH2OUT.createStatement();
            String queryH2OUT = "SELECT * FROM H2OUT";
            int in_node,out_node;
            double sumOUT2H=0,n0=0,n1=0,n2=0,n3=0,n4=0,value;
            


        for (int i = 0 ; i <hiddenNodes[0];i++){
                    ResultSet rsH2OUT = stmtIN2H.executeQuery(queryH2OUT);
                    //System.out.println("inside hidden Node "+hiddenNodes[0]);
                   // Have to add more loop for the number of inputs
                    while (rsH2OUT.next()){

                        out_node = rsH2OUT.getInt("out_node");
                        //System.out.println("out node " + out_node);
                        in_node = rsH2OUT.getInt("in_node");
                        //System.out.println("out node " + in_node);
                        value = rsH2OUT.getDouble("valueW");
                        //System.out.println("Value W " + value);
                       //for (int inNode=0; inNode<n;inNode++){
                        if (out_node == i){

                            if (in_node==0)
                            {
                                n0 = deltaK[0] * value;
                            }
                            if (in_node==1)
                            {
                                n1 = deltaK[1] * value;
                            }
                            if (in_node==2)
                            {
                                n2 = deltaK[2] * value;
                            }
                            if (in_node==3)
                            {
                                n3 = deltaK[3] * value;
                            }
                            if (in_node==4)
                            {
                                n4 = deltaK[4] * value;
                            }



                            }//for inNode

                       // }//if i the nodes in the first hidden layer

                    }//while rsIN2H
                    sumOUT2H = n0+n1+n2+n3+n4;
                    //System.out.println("Sum Before Activation "+sumIn2H);
                    //System.out.println("SumIn2H : = "+i+" ==> "+ activationFunction(sumIn2H));
                    //hiddenNodesValues[i][0]=0;//sumIn2H;
                    deltaJ[i][1] = sumOUT2H;
                    deltaJ[i][0]= sumOUT2H*activationFunction(sumOUT2H)*(1-activationFunction(sumOUT2H));
                    sumOUT2H=0;
                   // sumArrIn2H= null;
                    sumOUT2H=0;n0=0;n1=0;n2=0;n3=0;n4=0;
                    rsH2OUT.close();

                }// for the first hidden layer


         //       for (int i =0 ; i < hiddenNodesValues.length;i++){
                    //System.out.println("Hidden From Out Node "+i+" = "+deltaJ[i][0]);
         //       }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }// Calculate delta J

    public void calcDeltaWeight(){
       // System.out.println("inside calc Delta Weight");
        for (int i=0; i < deltaK.length; i++){
            for (int j = 0 ; j< hiddenNodesValues.length;j++){
                wDeltaB[i][j]= alpha*deltaK[i]*hiddenNodesValues[j][0];
                //System.out.println("Delta Wights : ("+i+","+j+") : "+wDeltaB[i][j]);
            }// for the number of hidden Nodes Values
        }// for the number of deltaK
    }//calc delta weight

    public void calcDeltaK(double [][] outNodes, double t){
        for (int i =0 ; i < outNodes.length; i++){
            deltaK[i]= (t - outNodes[i][1])*activationFunction(outNodes[i][0])*(1-activationFunction(outNodes[i][0]));
            //System.out.println("Delta K"+i+" = "+deltaK[i]);
        }
    }

   
    public double wRndGen(){
        return (Math.random()*0.5);
    }//wRndGen
    public void adjWB(){

        int counter= 0;
        int cntr=0;

        try{
            con = DriverManager.getConnection(url, "myLogin", "myPassword");
            stmtEx = con.createStatement();
        do{
            if(counter == 0 && P!=0){
                for (int i =0 ; i< n ; i ++){
                    for (int j=0 ; j < hiddenNodes[0];j++){
                       // arr[counter][i][j] = wRndGen();
                      // System.out.println("I2H V update : "+vDeltaB[j][i]);
                       wArrF [counter][i][j] = wArrF [counter][i][j] + vDeltaB[j][i];
                       stmtEx.executeUpdate("UPDATE IN2H SET valueW=" + wArrF [counter][i][j] + " WHERE (in_node = " +j +" AND out_node="+i+")");
                      // System.out.println("I2H After update : "+wArrF [counter][j][i]);
                    }
                }// input units
                //counter ++;
            }//if 0 ==> from input to hidden
            else if (counter == P && P!=0){
                for (int i =0 ; i< hiddenNodes[hiddenNodes.length -1] ; i ++){
                    for (int j=0 ; j < u ;j++){
                       // arr[counter][i][j] = wRndGen();
                       // System.out.println("H2O W before : "+wDeltaB[j][i]);
                        wArrF [counter][i][j] = wArrF [counter][i][j] + wDeltaB[j][i];
                        stmtEx.executeUpdate("UPDATE H2OUT SET valueW=" + wArrF [counter][i][j] + " WHERE (in_node = " +j +" AND out_node="+i+")");
                        //System.out.println("H2O After update : "+wArrF [counter][i][j]);

                    }
                }// input units
               // counter++;
            }// from hidden to output layer
            else{

                do{
                    if(hiddenNodes.length==0){
                        break;
                    }
                    else if (cntr < hiddenNodes.length-1)
                    for (int i =0 ; i< hiddenNodes[cntr]; i++){
                        for(int j =0 ; j<hiddenNodes[cntr+1];j++){
                           // arr[counter+cntr][i][j] = wRndGen();

                        }

                    }
                    cntr++;
                    //System.out.println("Inside cntr "+cntr);
                }while(cntr< hiddenNodes.length-1);
            }
            counter++;
           // System.out.println("Inside Counter "+counter);
        }while (counter<=P);
        //System.out.println("---------------------------------Weigths Updated");
        con.close();
        }catch (Exception s){System.out.println("Err"+ s.getMessage());}



      
    }//adject weights

    public double sqrdErr(double t, double [][] err){
        double sum=0;
        for (int i=0; i<u;i++){
            //System.out.println("sum :"+sum);
            //System.out.println("t :"+t);
            //System.out.println("err :"+err[i][1]);
            sum+= (t-err[i][1])*(t-err[i][1]);
        }
        System.out.println("Error : "+sum);
        return sum;
    }

    public void adjB(int y){
        System.out.print("Old bias = "+ bias+"||");
        bias = bias + y*alpha;
        System.out.println("New bias = "+ bias);
    }

    public boolean testWeightIn(){
        return true;
    }//test weights for changing

    public double activationFunction(double x){
        return 1/(1+Math.exp(-1*x));
    }//activation function


    public static void main(String[] args) {
       BP man = new BP();
       man.trainF();

    }

}
