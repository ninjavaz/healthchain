package com.healthchain.backend.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.util.NetworkProperties;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private NetworkProperties networkProps;

    public <T> T evaluateTransaction(TransactionType transactionType,
                                      Class<T> resultClass,
                                      String hospName,
                                      CustomIdentity customIdentity) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();
        return gson.fromJson(new String(contract.evaluateTransaction(transactionType.getValue())), resultClass);

    }

    public <T> List<T> evaluateTransactionWithList(TransactionType transactionType,
                                                 Class<T> resultClass,
                                                 String hospName,
                                                 CustomIdentity customIdentity) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();
        String result = new String(contract.evaluateTransaction(transactionType.getValue()));
        System.out.println("RESULT_EVALUATE");
        System.out.println(result);

        Type arrayType = TypeToken.getArray(resultClass).getType();
        T[] array = gson.fromJson(result, arrayType);

//                Type listType = new TypeToken<List<resultClass>>(){}.getType();
//        List<resultClass> resultList = gson.fromJson(result, listType);

//        List<resultClass> resultList = Arrays.asList(gson.fromJson(result, resultClass[].class));

        // Używając refleksji do stworzenia tablicy 'resultClass'
//        Object array = Array.newInstance(resultClass, 1); // Tymczasowo tworzymy tablicę z jednym elementem
//        T[] array = gson.fromJson(result, (Type) Array.newInstance(resultClass, 0).getClass()); // Deserializujemy JSON do tej tablicy


        // Konwertujemy tablicę na listę

//        // Now you can use the 'people' list
//        for (Person person : people) {
//            System.out.println("Name: " + person.getName() + ", Age: " + person.getAge());
//        }
        return Arrays.asList(array);

    }

    public <T> T submitTransaction(TransactionType transactionType,
                                  Class<T> resultClass,
                                  String hospName, CustomIdentity customIdentity, Object... args) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Gateway.Builder builder = Gateway.createBuilder();

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        // create a gateway connection
        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();

//        String[] argsJson = gson.toJson(arg);
        // Convert object to JSON string
        String[] argsJson = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            argsJson[i] = gson.toJson(args[i]);
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA");
            System.out.println(gson.toJson(args[i]));
        }

        System.out.println(argsJson);
        byte[] result = contract.submitTransaction(transactionType.getValue(), argsJson);

        System.out.println("DEBUG_SUBMIT_TRANSACTION");
        System.out.println(new String(result));
        System.out.println(gson.fromJson(new String(result), resultClass));

        return gson.fromJson(new String(result), resultClass);
    }
}
