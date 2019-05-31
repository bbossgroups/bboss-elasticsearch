(function() {
  function embed () {
    var evt = new Event('codefund');

    function trackUplift() {

    };

    function detectUplift(count) {

    }

    try {
      var targetElement = document.getElementById('codefund');
      if (targetElement == null) { targetElement = document.getElementById('codefund_ad'); }
      targetElement.innerHTML = '<div id="cf" style="max-width: 330px; margin: 0 auto;"> <div class="cf-wrapper" style="display: block; overflow: hidden; font-size: 14px; line-height: 1.4; font-family: Helvetica; padding: 15px;" align="left"> <div class="clearfix" style="overflow: auto;"> <a data-href="campaign_url" class="cf-img-wrapper" target="_blank" rel="noopener" style="float: left; margin-right: 15px;"> <img class="cf-img" src="images/logo2.png" style="vertical-align: middle; max-width: 130px; border: none;"> </a> <a data-href="campaign_url" class="cf-text" target="_blank" rel="noopener" style="color: #333; text-decoration: none;"> <strong>基于Bboss</strong><span>快速构建高效、可靠、安全的Elasticserach全文检索以及统计分析应用</span><br><strong>本网站中所有文档和Demo案例皆适用于Elasticsearch各个版本</strong> </a> </div> </div> </div> <style>#cf .clearfix::after { content: ""; clear: both; display: table; } </style>';
      //targetElement.querySelector('img[data-src="impression_url"]').src = 'https://codefund.app/display/1928f223-8cd8-49ad-a725-4afd8513b9a7.gif?template=docsify&theme=light';
      //targetElement.querySelector('a[data-target="powered_by_url"]').href = 'https://codefund.app/invite/aaq8ITeum7E';
      //targetElement.querySelectorAll('a[data-href="campaign_url"]').forEach(function (a) { a.href = 'https://codefund.app/impressions/1928f223-8cd8-49ad-a725-4afd8513b9a7/click?campaign_id=159'; });
      evt.detail = { status: 'ok', house: false };
      detectUplift(1);
    } catch (e) {
      console.log('CodeFund detected an error! Please verify an element exists with id="codefund". ' + e.message);
      evt.detail = { status: 'error', message: e.message };
    }
    document.removeEventListener('DOMContentLoaded', embed);
    window.dispatchEvent(evt);
  };
  (document.readyState === 'loading') ? document.addEventListener('DOMContentLoaded', embed) : embed();
})();
