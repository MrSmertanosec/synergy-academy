package queries;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class SqlQueries {
    public static String getAllQuery(String tableName) {
        return new QueryBuilder("select")
                .all()
                .addFromTable(tableName)
                .getQuery();
    }

    public static String getIdQuery(String tableName, String id) {
        return "select * from public.getstudentbyid(" + id + ")";
    }

    public static String getFunctionQuery(String function, String... params) {
        return new QueryBuilder("select")
                .all()
                .addFromFunction(function, params)
                .getQuery();
    }

    public static String updateQuery(JsonArray metadata, String id, String tableName, JsonObject data) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("update")
                .addTable(tableName);

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }
//todo
            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addFilterSetColumn(modifyClassifierTable(currentColumn),
                        currentColumn, currentColumn);
            } else {
                queryBuilder.addFilterSet(currentColumn, data.getValue(currentColumn));
            }
        }

        for (String classifier : classifiers) {
            queryBuilder.addFromTable(modifyClassifierTable(classifier));
        }

        for (String classifier : classifiers) {
            queryBuilder.addFilterWhere(modifyClassifierTable(classifier),
                    "name", data.getValue(classifier));
        }

        return queryBuilder.addFilterWhere(tableName, tableName + "id", id).getQuery();
    }

    public static String insertQuery(JsonArray metadata, String tableName, JsonObject data) {
        QueryBuilder queryBuilder = new QueryBuilder("insert into")
                .addTable(tableName)
                .openBrace();

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }

            queryBuilder.append(currentColumn);

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            } else {
                queryBuilder.closeBrace();
            }
        }

        return queryBuilder.append(selectQuery(metadata, tableName, data)).getQuery();
    }

    public static String selectQuery(JsonArray metadata, String tableName, JsonObject data) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("select");

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (!data.containsKey(currentColumn)) {
                continue;
            }

            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addColumn(modifyClassifierTable(currentColumn), currentColumn);
            } else {
                queryBuilder.appendData(data.getValue(currentColumn));
            }

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            }
        }

        for (String classifier : classifiers) {
            queryBuilder.addFromTable(modifyClassifierTable(classifier));
        }

        for (String classifier : classifiers) {
            queryBuilder.addFilterWhere(modifyClassifierTable(classifier),
                    "name", data.getValue(classifier));
        }

        return queryBuilder.getQuery();
    }


    public static String selectQuery(JsonArray metadata, String tableName) {
        List<String> classifiers = new ArrayList<>();

        QueryBuilder queryBuilder = new QueryBuilder("select");

        for (int i = 0; i < metadata.size(); ++i) {
            String currentColumn = metadata.getJsonObject(i).getString("column_name");

            if (metadata.getJsonObject(i).getBoolean("isclassiferid")) {
                classifiers.add(currentColumn);
                queryBuilder.addColumn(modifyClassifierTable(currentColumn), currentColumn);
            } else {
                queryBuilder.addColumn(tableName, currentColumn);
            }

            if (i < metadata.size() - 1) {
                queryBuilder.appendComma();
            }
        }

        for (String classifier : classifiers) {
            queryBuilder.addFromTable(modifyClassifierTable(classifier));
        }

        for (String classifier : classifiers) {
            queryBuilder.addFilterWhereTable(tableName, classifier, modifyClassifierTable(classifier), classifier);
        }

        return queryBuilder.getQuery();
    }

    private static String modifyClassifierTable(String classifierColumn) {
        return "c_" + classifierColumn.substring(0, classifierColumn.length() - 2);
    }

//    insert into student(firstname,lastname,email,fullname,phonenumber,
//                        address,universityid,gpa,whatprogramminglanguagesdoyouknow,
//                        educationdepartmentadmissionandgraduationyear,othercoursesattended,
//                        haveyoueverparticipatedinprogramming,doyouhaveworkexperience,howdidyoufindid,birthday)
//    select 'myname', 'yourname', 'myemail', 'myfull', 'myphonenumber', 'myaddress', public.c_university."universityid",
//            4, 'mywhatprogramminglanguagesdoyouknow', 'myeducationdepartmentadmissionandgraduationyear',
//            'myothercoursesattended', 'myhaveyoueverparticipatedinprogramming', 'mydoyouhaveworkexperience',
//    c_howdidyoufind.howdidyoufindid, '2022-12-12'
//    from c_university, c_howdidyoufind
//    where c_university."name" = 'Yerevan State University (1919)'
//    and c_howdidyoufind."name" = 'Instagram'
}
