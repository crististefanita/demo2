package atf.api.reporting.runtime.core;

/** Runtime controller for the diagnostic Fails projection and deep-links. */
public final class ExtentReportFailureRuntime {
    private ExtentReportFailureRuntime() { }

    public static String script() {
        return String.join("",
                statusDisplayScript(),
                failedSetupEvidenceScript(),
                failedUiEvidenceScript(),
                failureStateScript(),
                failedAssertionRowsScript(),
                failureContextForScript(),
                failureIndexMarkupScript(),
                selectFailureScript(),
                renderFailsScript());
    }

    private static String statusDisplayScript() {
        return "function statusDisplay(value){var key=String(value||'').trim(),map={'200':'200 OK','201':'201 Created','202':'202 Accepted','204':'204 No Content','400':'400 Bad Request','401':'401 Unauthorized','403':'403 Forbidden','404':'404 Not Found','409':'409 Conflict','500':'500 Internal Server Error'};return map[key]||key;}";
    }

    private static String failedSetupEvidenceScript() {
        return "function failedSetupEvidence(card){var zone=card.querySelector('.atf-setup-evidence[data-atf-evidence-status=\"fail\"]');if(!zone)return null;var rows=q('tbody tr',zone),row=rows.find(function(candidate){return String(candidate.textContent||'').indexOf('❌')>=0;});if(!row)return null;var cells=Array.prototype.slice.call(row.cells||[]).map(function(cell){return text(cell,'');});return {ui:true,step:'Setup',validation:'Setup evidence',assertion:'Setup check · '+(cells[0]||'unknown')+' → '+(cells[1]||'')+' → '+(cells[2]||''),message:'Setup check failed: '+(cells[0]||'unknown')+' expected '+(cells[1]||'')+', actual '+(cells[2]||'')};}";
    }

    private static String failedUiEvidenceScript() {
        return "function failedUiEvidence(card){var zones=q('details[data-atf-evidence-status=\"fail\"], .atf-ui-assertions[data-atf-evidence-status=\"fail\"], details.step-detail.assertion-evidence',card);for(var index=0;index<zones.length;index++){var zone=zones[index],rows=q('tbody tr',zone),row=rows.find(function(candidate){return !!candidate.querySelector('.atf-ui-action-result-fail')||String(candidate.textContent||'').indexOf('❌')>=0;});if(!row)continue;var cells=Array.prototype.slice.call(row.cells||[]).map(function(cell){return text(cell,'');}),observation=zone.classList.contains('atf-ui-observations'),assertion=zone.classList.contains('atf-ui-assertions')||zone.classList.contains('assertion-evidence'),label=observation?'Audit observation':assertion?'UI assertion':'Playwright action';return {ui:true,step:label,validation:text(card,'.card-title a.node'),assertion:label+' · '+(cells[0]||'unknown')+' → '+(cells[1]||'')+' → '+(cells[2]||'')+' → '+(cells[3]||''),message:label+' failed: '+(cells[3]||'see the concrete evidence row')};}return null;}";
    }

    /**
     * Publishes small collection/derivation helpers and one authoritative failure-context owner.
     * The function deliberately orchestrates helpers; DOM reads and projection details live in
     * the helpers so the generated runtime has a stable seam for static and browser contracts.
     */
    private static String failureStateScript() {
        return String.join("",
                "function failureTransportEvidence(card,state){var trace=card.querySelector('.helm-trace-table'),execution=card.querySelector('.execution-table');[trace,execution].filter(function(table){return !!table;}).forEach(function(evidence){var evidenceRows=evidence.tBodies&&evidence.tBodies[0]?Array.prototype.slice.call(evidence.tBodies[0].rows):[];evidenceRows.forEach(function(evidenceRow){var evidenceCells=Array.prototype.slice.call(evidenceRow.cells||[]),evidenceLabel=String(text(evidenceCells[0],'')).toUpperCase();if(evidenceLabel==='FAILURE CAUSE')state.message=text(evidenceCells[1],'');if((evidenceLabel==='RESPONSE DURATION'||evidenceLabel==='RESPONSE DURATION (MS)'||evidenceLabel==='RESPONSE.DURATION.MS')&&!state.duration){var rawDuration=text(evidenceCells[1],'');var durationMs=atfDurationValue(rawDuration);state.duration=durationMs===null?rawDuration:atfFormatDuration(durationMs);}});});}",
                "function failureValidationCandidate(card){var table=card.querySelector('.helm-validation-table');if(!table)return null;var body=table.tBodies&&table.tBodies[0],rows=body?Array.prototype.slice.call(body.rows):[],row=rows.find(function(candidate){var cells=Array.prototype.slice.call(candidate.cells||[]);return text(cells[3],'').indexOf('❌')>=0;})||rows[0];if(!row)return null;var cells=Array.prototype.slice.call(row.cells||[]);return {card:card,label:text(cells[0],''),expected:text(cells[1],''),actual:text(cells[2],''),result:text(cells[3],'')};}",
                "function collectFailureState(item){var state={phase:'',phaseCard:null,requests:[],validations:[],uiFailures:[],message:'',duration:''};q('.card',item).forEach(function(card){var title=text(card,'.card-title a.node'),upper=title.toUpperCase();if(!state.phase&&/^(SETUP|TEST BODY|TEARDOWN)$/.test(upper)&&statusOf(card)==='fail'){state.phase=title;state.phaseCard=card;}if(/^(GET|POST|PUT|PATCH|DELETE|HEAD) /.test(upper))state.requests.push({card:card,title:title});var setupFailure=failedSetupEvidence(card),uiFailure=failedUiEvidence(card);if(setupFailure)state.uiFailures.push(setupFailure);if(uiFailure)state.uiFailures.push(uiFailure);failureTransportEvidence(card,state);var candidate=failureValidationCandidate(card);if(candidate)state.validations.push(candidate);});return state;}",
                "function failureEntryBelongsToPhase(entry,phaseCard){return !phaseCard||!!(phaseCard.compareDocumentPosition(entry.card)&Node.DOCUMENT_POSITION_PRECEDING);}",
                "function latestFailureEntry(entries,phaseCard){var scoped=entries.filter(function(entry){return failureEntryBelongsToPhase(entry,phaseCard);});return scoped.length?scoped[scoped.length-1]:(entries[0]||null);}",
                "function failureContextFromState(state){var request='',validation='',assertion='',message=state.message,fallback=null;if(state.phase!=='TEARDOWN'){var requestEntry=latestFailureEntry(state.requests,state.phaseCard);request=requestEntry?requestEntry.title:'';fallback=latestFailureEntry(state.validations,state.phaseCard);if(fallback){validation=/status/i.test(fallback.label)?'Validate: status='+fallback.expected:fallback.label;assertion=fallback.label+' → '+statusDisplay(fallback.expected)+' → '+statusDisplay(fallback.actual);if(!message&&((fallback.result||'').indexOf('❌')>=0||state.phase==='TEST BODY'))message=fallback.label+' expected '+statusDisplay(fallback.expected)+', actual '+statusDisplay(fallback.actual);}}var context={phase:state.phase||'Unknown phase',ui:false,request:request,duration:state.duration,step:'',validation:validation||'No contract validation recorded',assertion:assertion||'Failure recorded; see exact test for full evidence',message:message||'Failure recorded; see exact test for full evidence'};if(state.uiFailures.length){var concrete=state.uiFailures[0];context.ui=true;context.step=concrete.step||'UI step';context.request='';context.duration='';context.validation=concrete.validation;context.assertion=concrete.assertion;context.message=concrete.message;}return context;}",
                "function failureContext(item){var context=failureContextFromState(collectFailureState(item));if(typeof scopeFailureContextToPhase==='function')context=scopeFailureContextToPhase(item,context);if(typeof failureCauseChain==='function'){var chain=failureCauseChain(item);if(chain)context.message=chain;}return context;}"
        );
    }

    private static String failedAssertionRowsScript() {
        return "function failedAssertionRows(item){return q('.helm-validation-table tbody tr',item).filter(function(row){var result=String(row.textContent||'').toLowerCase(),status=row.getAttribute('data-atf-assertion-status')||'';return status==='fail'||result.indexOf('❌')>=0||!!row.querySelector('.badge-danger');});}";
    }

    private static String failureContextForScript() {
        return "function failureContextFor(item,anchor){var ctx=failureContext(item);if(!anchor)return ctx;var cells=Array.prototype.slice.call(anchor.cells||[]).map(function(cell){return text(cell,'');}),label=cells[0]||'Assertion',expected=cells[1]||'',actual=cells[2]||'';var validation=anchor.closest('.card');if(validation){var title=text(validation,'.card-title a.node');if(title)ctx.validation=title;}ctx.assertion=label+' → '+statusDisplay(expected)+' → '+statusDisplay(actual);if(!ctx.message||ctx.message.indexOf('PRIMARY:')<0)ctx.message=label+' expected '+statusDisplay(expected)+', actual '+statusDisplay(actual);return ctx;}";
    }

    private static String failureIndexMarkupScript() {
        return "function failureIndexMarkup(name,ctx,id,scenarioIndex,ordinal){var safeId=esc(id),safeScenario=esc(scenarioIndex);return '<div class=\"atf-failure-index atf-failure-evidence\" data-atf-failure-id=\"failure-'+safeId+'-'+safeScenario+'-'+esc(ordinal||1)+'\"><h4 class=\"atf-failure-index-title\">'+esc(name)+'</h4>'"
                + "+'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">TEST</span><span class=\"atf-failure-value\">'+esc(ctx.owner||'API tests')+'</span></div>'"
                + "+'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">PHASE</span><span class=\"atf-failure-value\">'+esc(ctx.phase)+'</span></div>'"
                + "+'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">'+(ctx.ui?'STEP':'REQUEST')+'</span><span class=\"atf-failure-value\">'+esc(ctx.ui?ctx.step:ctx.request)+'</span></div>'"
                + "+(ctx.ui?'':'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">DURATION</span><span class=\"atf-failure-value\">'+esc(ctx.duration)+'</span></div>')"
                + "+'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">VALIDATION</span><span class=\"atf-failure-value\">'+esc(ctx.validation)+'</span></div>'"
                + "+'<div class=\"atf-failure-index-row\"><span class=\"atf-failure-label\">ASSERTION</span><span class=\"atf-failure-value\">'+esc(ctx.assertion)+'</span></div>'"
                + "+'<div class=\"atf-failure-index-row atf-failure-index-cause\"><span class=\"atf-failure-label\">CAUSE</span><span class=\"atf-failure-value\">'+esc(ctx.message)+'</span></div><a class=\"atf-failure-open\" href=\"#test-id='+safeId+'&amp;scenario-index='+safeScenario+'\" title=\"Open the exact failing test in the Tests tab\" data-atf-test-id=\"'+safeId+'\" data-atf-scenario-index=\"'+safeScenario+'\" data-atf-failure-ordinal=\"'+esc(ordinal||1)+'\" onclick=\"if(typeof atfOpenTest===\\'function\\'){try{atfOpenTest(\\''+safeId+'\\',\\''+safeScenario+'\\');return false;}catch(error){}}window.location.hash=\\'test-id=\\'+encodeURIComponent(\\''+safeId+'\\')+\\'&scenario-index=\\'+encodeURIComponent(\\''+safeScenario+'\\');return false;\">Open in Tests</a></div>'; }";
    }

    private static String selectFailureScript() {
        return "function selectFailure(index){var rows=q('#atf-failure-list>.test-item'),row=rows[index],detail=document.getElementById('atf-failure-detail');if(!row||!detail)return;for(var i=0;i<rows.length;i++)rows[i].classList.toggle('active',i===index);var id=row.getAttribute('data-atf-test-id'),scenarioIndex=row.getAttribute('data-atf-scenario-index');detail.innerHTML='';var indexMarkup=row.getAttribute('data-atf-index-html')||'';if(indexMarkup)detail.insertAdjacentHTML('beforeend',indexMarkup);var open=detail.querySelector('.atf-failure-open');if(open)open.addEventListener('click',function(e){e.preventDefault();e.stopPropagation();if(typeof atfOpenTest==='function'){try{atfOpenTest(id,scenarioIndex);return false;}catch(error){}}window.location.hash='test-id='+encodeURIComponent(id)+'&scenario-index='+encodeURIComponent(scenarioIndex);return false;});}";
    }

    private static String renderFailsScript() {
        return String.join("",
                "function failureRecords(){var records=[];testItems().filter(function(item){return statusOf(item)==='fail';}).forEach(function(item){var failedRows=failedAssertionRows(item);if(!failedRows.length)failedRows=[null];failedRows.forEach(function(assertionRow,ordinal){records.push({item:item,assertionRow:assertionRow,ordinal:ordinal+1});});});return records;}",
                "function failurePhaseRank(phase){var order={'SETUP':0,'TEST BODY':1,'TEARDOWN':2},rank=order[phase];return rank===undefined?99:rank;}",
                "function sortFailureRecords(records){return records.sort(function(a,b){return failurePhaseRank(failureContextFor(a.item,a.assertionRow).phase)-failurePhaseRank(failureContextFor(b.item,b.assertionRow).phase);});}",
                "function resetFailureProjection(){var host=document.getElementById('atf-failure-list'),detail=document.getElementById('atf-failure-detail');if(host)host.innerHTML='';if(detail)detail.innerHTML='';return host;}",
                "function failureRow(record,index){var item=record.item,id=item.getAttribute('data-atf-class-root-id')||item.getAttribute('test-id'),scenarioIndex=item.getAttribute('data-atf-scenario-index')||'0',name=testName(item),owner=testClass(item),ctx=failureContextFor(item,record.assertionRow);ctx.owner=owner;var row=document.createElement('div');row.className='test-item atf-failure';row.setAttribute('status','fail');row.setAttribute('data-atf-test-id',id);row.setAttribute('data-atf-scenario-index',scenarioIndex);row.setAttribute('data-atf-failure-ordinal',String(record.ordinal));row.setAttribute('data-atf-failure-id','failure-'+id+'-'+scenarioIndex+'-'+record.ordinal);row.setAttribute('data-atf-failure-phase',ctx.phase);row.setAttribute('data-atf-index-html',failureIndexMarkup(name,ctx,id,scenarioIndex,record.ordinal));row.innerHTML='<div class=\"test-detail\"><p class=\"name\">'+esc(name)+'</p><p class=\"text-sm\">'+esc(owner)+'</p></div><div class=\"test-status\"><span class=\"badge log badge-danger\">Fail</span></div>';row.addEventListener('click',function(){selectFailure(index);});return row;}",
                "function renderFails(){var host=document.getElementById('atf-failure-list');if(!host)return;var records=sortFailureRecords(failureRecords());resetFailureProjection();if(!records.length){host.innerHTML='<div class=\"atf-fails-empty\">No failed tests.</div>';return;}records.forEach(function(record,index){host.appendChild(failureRow(record,index));});selectFailure(0);};"
        );
    }

    /** Adds cause-chain helpers after the failure runtime without rebinding failureContext. */
    public static String causeChainScript() {
        return "function failureCauseChain(item){var causes=[];q('.card[data-atf-node-role=\"phase\"]',item).forEach(function(card){if(statusOf(card)!=='fail')return;q('.helm-trace-table tbody tr',card).forEach(function(row){var cells=Array.prototype.slice.call(row.cells||[]),label=String(text(cells[0],'')).trim().toUpperCase();if(label==='FAILURE CAUSE')causes.push(text(cells[1],''));});});return causes.map(function(cause,index){return (index===0?'PRIMARY: ':'SECONDARY: ')+cause;}).join(' | ');}function scopeFailureContextToPhase(item,context){var phase=q('.card[data-atf-node-role=\"phase\"]',item).find(function(card){return statusOf(card)==='fail';});if(!phase)return context;var requests=q('[data-atf-node-role=\"request\"]',phase),validations=q('[data-atf-node-role=\"validation\"]',phase);if(!context.ui&&requests.length){var request=requests[requests.length-1];context.request=text(request,'.card-title a.node');var durationRow=q('.helm-trace-table tbody tr,.execution-table tbody tr',request).find(function(row){var label=String(text(row.cells&&row.cells[0],'')).trim().toUpperCase();return label==='RESPONSE DURATION'||label==='RESPONSE DURATION (MS)'||label==='RESPONSE.DURATION.MS';});if(durationRow){var rawDuration=text(durationRow.cells[1],'');var durationMs=atfDurationValue(rawDuration);context.duration=durationMs===null?rawDuration:atfFormatDuration(durationMs);}}if(validations.length)context.validation=text(validations[validations.length-1],'.card-title a.node');return context;}";
    }
}
