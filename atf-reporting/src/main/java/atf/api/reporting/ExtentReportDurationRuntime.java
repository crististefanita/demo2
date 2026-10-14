package atf.api.reporting;

/** Runtime controller for duration normalization and class-level aggregation. */
final class ExtentReportDurationRuntime {
    private ExtentReportDurationRuntime() { }

    static String script() {
        return "function aggregateClassDurations(){classItems().forEach(function(root){var cards=isClassRoot(root)?directScenarioCards(root):[],total=0,count=0;cards.forEach(function(card){var duration=card.querySelector(':scope > .card-header .node-info .badge-default'),value=atfDurationValue(text(duration,''));if(value!==null){total+=value;count++;if(duration)duration.setAttribute('data-duration-source','measured');}});if(!count)return;var labels=q('.test-detail .text-sm > span',root).filter(function(e){return !e.classList.contains('badge');});if(labels.length>1){labels[labels.length-1].textContent=String(total)+' ms';labels[labels.length-1].setAttribute('data-duration-ms',String(total));labels[labels.length-1].setAttribute('data-duration-source','aggregated');}var detail=root.querySelector('.detail-head .info > .badge-default:not(.uri-anchor):not(.pointer)');if(detail){detail.textContent=String(total)+' ms';detail.setAttribute('data-duration-ms',String(total));detail.setAttribute('data-duration-source','aggregated');}root.setAttribute('data-duration-ms',String(total));root.setAttribute('data-duration-source','aggregated');});}"
                + "var atfBaseActivate=window.atfActivate;window.atfActivate=function(id){atfBaseActivate(id);aggregateClassDurations();};"
                + "document.addEventListener('DOMContentLoaded',function(){setTimeout(aggregateClassDurations,0);});"
                + "window.addEventListener('load',function(){setTimeout(aggregateClassDurations,0);});";
    }
}
