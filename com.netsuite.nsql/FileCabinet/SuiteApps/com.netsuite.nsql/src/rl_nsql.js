/**
 *
 * @NApiVersion 2.1
 * @NScriptType Restlet
 * @NModuleScope SameAccount
 */
define(['N/query'], function(_nQuery) {

  function doPost(req) {

    if( !req.query ) {
      log.debug('Error', 'Incomplete request: ' + req);
      return "Incomplete request: " + req;
    }

    var reqQuery = req.query;
    log.audit('request SuiteQL Query', reqQuery);
    var result = [];
    var resultSet = _nQuery.runSuiteQL({ query: reqQuery });
    for (var i = 0; i < resultSet.results.length; i++) {
      result.push(resultSet.results[i].asMap());
    }
    return result;

  }

  return {
    post: doPost
  };

});
